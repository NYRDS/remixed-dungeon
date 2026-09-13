#!/usr/bin/env python3
"""Render announce/promo images via the ComfyUI on hdd_maxi1 (yggdrasil).

Base: Qwen-Image-2512 fp8 + qwen2.5-vl-7b TE + qwen_image_vae (proven graph
from the instance's own history). Prompts composed from
docs/txt2img_promo_library.md — paste shared blocks verbatim.

True pixel art = pixel-art LoRA at render time + deterministic post-process
(nearest-neighbor downscale to pixel grid + palette quantize + nearest upscale
back). No AI upscalers.

Examples:
  python3 comfy_announce.py prompt.txt --fast --lora PixelArt_Redmond_qwen.safetensors:0.8
  python3 comfy_announce.py "STYLE: ... SCENE: ..." --size 1664x832 --pixel 4
  python3 comfy_announce.py --status
"""
import argparse
import json
import os
import random
import subprocess
import sys
import time
import urllib.request
import urllib.parse
from pathlib import Path

DEFAULT_URL = "http://[201:b5b5:f6b:5ac2:1734:21ad:6eb3:2d33]:8188"

UNET = "qwen_image_2512_fp8_e4m3fn.safetensors"
CLIP = "qwen_2.5_vl_7b_fp8_scaled.safetensors"
VAE = "qwen_image_vae.safetensors"
LIGHTNING = "Qwen-Image-Lightning-4steps-V1.0.safetensors"

NEGATIVE = ("blurry, lowres, jpeg artifacts, text, watermark, signature, logo, letters, "
            "numbers, photorealistic, 3d render, smooth gradients, antialiasing, deformed "
            "hands, extra limbs, oversaturated, neon colors")


HOG_CODE = """
import sys, time, torch
x = torch.empty(int(float(sys.argv[1]) * 2**30), dtype=torch.uint8, device="cuda:0")
x.fill_(1)
print("hog up", flush=True)
while True:
    time.sleep(60)
"""


class VramReserve:
    """Hold `gb` on cuda:0 during the render: ComfyUI sees less free VRAM and
    switches to partial-load streaming, leaving headroom for lora patches."""

    def __init__(self, gb):
        self.gb = gb
        self.proc = None

    def __enter__(self):
        if not self.gb:
            return self
        self.proc = subprocess.Popen([sys.executable, "-c", HOG_CODE, str(self.gb)],
                                     stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
        line = self.proc.stdout.readline()
        if "hog up" not in line:
            print(f"reserve {self.gb}GB failed: {line.strip()}", file=sys.stderr)
            self.proc = None
        else:
            print(f"reserved {self.gb}GB on cuda:0", file=sys.stderr)
            time.sleep(2)
        return self

    def __exit__(self, *a):
        if self.proc:
            self.proc.terminate()
            try:
                self.proc.wait(10)
            except subprocess.TimeoutExpired:
                self.proc.kill()
            print("reserve released", file=sys.stderr)


def api(url, path, payload=None, timeout=30):
    req = urllib.request.Request(url.rstrip("/") + path,
                                 data=json.dumps(payload).encode() if payload else None,
                                 headers={"Content-Type": "application/json"})
    with urllib.request.urlopen(req, timeout=timeout) as r:
        return json.loads(r.read())


def build_graph(args, seed):
    g = {
        "1": {"class_type": "UNETLoader", "inputs": {"unet_name": args.unet, "weight_dtype": "default"}},
        # TE on cpu: frees ~8GB VRAM for the 20B UNET on the shared GPU
        "2": {"class_type": "CLIPLoader", "inputs": {"clip_name": CLIP, "type": "qwen_image", "device": "cpu"}},
        "3": {"class_type": "VAELoader", "inputs": {"vae_name": VAE}},
        "4": {"class_type": "CLIPTextEncode", "inputs": {"text": args.prompt, "clip": ["2", 0]}},
        "5": {"class_type": "CLIPTextEncode", "inputs": {"text": args.negative, "clip": ["2", 0]}},
        "7": {"class_type": "EmptySD3LatentImage",
              "inputs": {"width": args.width, "height": args.height, "batch_size": 1}},
    }
    model = ["1", 0]
    loras = list(args.lora or [])
    if args.fast:
        loras.insert(0, f"{LIGHTNING}:1.0")
    for i, spec in enumerate(loras):
        name, _, w = spec.partition(":")
        nid = f"l{i}"
        g[nid] = {"class_type": "LoraLoaderModelOnly",
                  "inputs": {"lora_name": name, "strength_model": float(w or 1.0), "model": model}}
        model = [nid, 0]
    g["6"] = {"class_type": "ModelSamplingAuraFlow", "inputs": {"model": model, "shift": 3.1}}
    g["8"] = {"class_type": "KSampler",
              "inputs": {"seed": seed, "steps": args.steps, "cfg": args.cfg,
                         "sampler_name": "euler", "scheduler": "simple", "denoise": 1.0,
                         "model": ["6", 0], "positive": ["4", 0], "negative": ["5", 0],
                         "latent_image": ["7", 0]}}
    g["9"] = {"class_type": "VAEDecode", "inputs": {"samples": ["8", 0], "vae": ["3", 0]}}
    g["10"] = {"class_type": "SaveImage", "inputs": {"filename_prefix": args.prefix, "images": ["9", 0]}}
    return g


def submit(url, graph):
    # release cached models/blocks first: the shared GPU runs other processes
    try:
        urllib.request.urlopen(urllib.request.Request(
            url.rstrip("/") + "/free",
            data=b'{"unload_models": true, "free_memory": true}',
            headers={"Content-Type": "application/json"}), timeout=60).read()
    except Exception as e:
        print(f"/free skipped: {e}", file=sys.stderr)
    return api(url, "/prompt", {"prompt": graph, "client_id": "comfy_announce_py"})


def vram_free_gb(url):
    d = api(url, "/system_stats")["devices"][0]
    return d["vram_free"] / 2**30


def render_once(url, args, seed):
    graph = build_graph(args, seed)
    print(f"submit: {args.width}x{args.height} steps={args.steps} cfg={args.cfg} seed={seed}",
          file=sys.stderr)
    r = submit(url, graph)
    print(f"prompt_id={r['prompt_id']}", file=sys.stderr)
    return wait(url, r["prompt_id"], args.wait)


def render(url, args, seed):
    """Render with VRAM gate + OOM retry: shared GPU, model leaves no headroom."""
    if args.need_vram:
        while True:
            f = vram_free_gb(url)
            if f >= args.need_vram:
                break
            print(f"  waiting for VRAM: {f:.1f}GB < {args.need_vram}GB", file=sys.stderr)
            time.sleep(30)
    for attempt in range(args.retries + 1):
        try:
            return render_once(url, args, seed)
        except RuntimeError as e:
            if attempt >= args.retries or "memory" not in str(e).lower():
                raise
            print(f"OOM ({e}), retry {attempt+1}/{args.retries} after 60s", file=sys.stderr)
            time.sleep(60)
    raise SystemExit("unreachable")


def wait(url, prompt_id, timeout, poll=5):
    t0 = time.time()
    while time.time() - t0 < timeout:
        h = api(url, f"/history/{prompt_id}", timeout=30)
        e = h.get(prompt_id)
        if e:
            st = e.get("status", {})
            if st.get("status_str") == "error":
                msg = ""
                for m in st.get("messages", []):
                    if m[0] == "execution_error":
                        d = m[1]
                        msg = f"{d.get('node_type')}[{d.get('node_id')}]: {d.get('exception_message')}"
                raise RuntimeError(msg)
            if e.get("outputs"):
                return e
        q = api(url, "/queue")
        if prompt_id in [x[1] for x in q["queue_running"]]:
            state = "running"
        elif prompt_id in [x[1] for x in q["queue_pending"]]:
            state = f"pending ({len(q['queue_pending'])} in queue)"
        else:
            state = "loading/sampling"
        print(f"  {int(time.time()-t0)}s {state}", file=sys.stderr)
        time.sleep(poll)
    raise SystemExit(f"timeout after {timeout}s")


def fetch(url, entry, outdir):
    outdir = Path(outdir)
    outdir.mkdir(parents=True, exist_ok=True)
    paths = []
    for node_out in entry.get("outputs", {}).values():
        for img in node_out.get("images", []):
            q = urllib.parse.urlencode({"filename": img["filename"], "subfolder": img["subfolder"],
                                        "type": img["type"]})
            dest = outdir / img["filename"]
            with urllib.request.urlopen(f"{url}/view?{q}", timeout=120) as r, open(dest, "wb") as f:
                f.write(r.read())
            paths.append(dest)
    return paths


def postprocess(src, pixel, colors):
    """True pixel art: nearest downscale by `pixel`, quantize, nearest upscale back."""
    from PIL import Image
    im = Image.open(src).convert("RGB")
    w, h = im.size
    small = im.resize((w // pixel, h // pixel), Image.NEAREST)
    if colors:
        small = small.quantize(colors=colors, method=Image.MEDIANCUT, dither=Image.NONE).convert("RGB")
    big = small.resize((w, h), Image.NEAREST)
    p = Path(src)
    sp = p.with_name(f"{p.stem}.pixel{pixel}.png")
    bp = p.with_name(f"{p.stem}.pixel{pixel}x{pixel}.png")
    small.save(sp)
    big.save(bp)
    return sp, bp


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("prompt", nargs="?", help="prompt text, or path to a .txt file")
    ap.add_argument("--url", default=os.environ.get("COMFY_URL", DEFAULT_URL))
    ap.add_argument("--size", default="1664x832", help="WxH, default banner 1664x832")
    ap.add_argument("--steps", type=int, default=None)
    ap.add_argument("--cfg", type=float, default=None)
    ap.add_argument("--seed", type=int, default=None)
    ap.add_argument("--fast", action="store_true", help="lightning 4-step lora, cfg 1.0")
    ap.add_argument("--lora", action="append", metavar="NAME[:W]", help="model-only lora, repeatable")
    ap.add_argument("--pixel", type=int, default=4, help="post-process downscale factor, 0=off")
    ap.add_argument("--colors", type=int, default=128, help="palette colors after quantize, 0=off")
    ap.add_argument("--negative", default=NEGATIVE)
    ap.add_argument("--out", default="promo_out")
    ap.add_argument("--prefix", default="announce")
    ap.add_argument("--unet", default=UNET, help="diffusion model file in models/diffusion_models")
    ap.add_argument("--reserve", type=float, default=0,
                    help="GB to hold on cuda:0 during render, forces ComfyUI partial-load (enables loras)")
    ap.add_argument("--need-vram", type=float, default=0, help="GB free required before submit, else wait")
    ap.add_argument("--retries", type=int, default=2, help="OOM resubmit attempts")
    ap.add_argument("--save", metavar="PATH", help="write the API-format workflow JSON and exit")
    ap.add_argument("--wait", type=int, default=3600)
    ap.add_argument("--status", action="store_true")
    args = ap.parse_args()

    url = args.url
    if args.status:
        print(json.dumps(api(url, "/system_stats")["system"], indent=1)[:400])
        q = api(url, "/queue")
        print(f"running={len(q['queue_running'])} pending={len(q['queue_pending'])}")
        return

    if not args.prompt:
        ap.error("prompt required unless --status")
    if Path(args.prompt).is_file():
        args.prompt = Path(args.prompt).read_text()
    w, h = (int(x) for x in args.size.lower().split("x"))
    args.width, args.height = w, h
    if args.fast:
        args.steps = args.steps or 4
        args.cfg = args.cfg or 1.0
    else:
        args.steps = args.steps or 32
        args.cfg = args.cfg or 4.0
    seed = args.seed if args.seed is not None else random.randint(0, 2**31 - 1)

    if args.save:
        Path(args.save).write_text(json.dumps(build_graph(args, seed), indent=1))
        print(args.save)
        return

    with VramReserve(args.reserve):
        entry = render(url, args, seed)
        paths = fetch(url, entry, args.out)
    for p in paths:
        print(p)
    if args.pixel:
        for p in paths:
            for q in postprocess(p, args.pixel, args.colors):
                print(q)


if __name__ == "__main__":
    main()
