#!/usr/bin/env python3
"""Bake a lora into an fp8 safetensors diffusion model (CPU, streamed).

Why: ComfyUI's ModelPatcher double-counts the clone's resident weights when a
lora is attached (load budget >= full model -> always full-loads -> OOM on a
GPU shared with other processes). A merged checkpoint loads like the base
model, which works.

  python3 merge_lora_fp8.py BASE.safetensors LORA.safetensors OUT.safetensors [--strength 1.0]

Lora strength: baked permanently; render with the OUT model via UNETLoader,
no LoraLoader node.
"""
import argparse
import json
import shutil
import struct
import sys
from pathlib import Path

import torch
from safetensors.torch import safe_open

DTYPES = {"F8_E4M3": torch.float8_e4m3fn, "BF16": torch.bfloat16,
          "F16": torch.float16, "F32": torch.float32}
TORCH2ST = {v: k for k, v in DTYPES.items()}


def read_header(path):
    with open(path, "rb") as f:
        n = struct.unpack("<Q", f.read(8))[0]
        return json.loads(f.read(n)), 8 + n


def main():
    ap = argparse.ArgumentParser(description=__doc__,
                                 formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("base")
    ap.add_argument("lora")
    ap.add_argument("out")
    ap.add_argument("--strength", type=float, default=1.0)
    args = ap.parse_args()

    base_hdr, base_data0 = read_header(args.base)
    deltas = {}
    with safe_open(args.lora, framework="pt", device="cpu") as f:
        lora_keys = [k for k in f.keys() if k != "__metadata__"]
        for k in lora_keys:
            if ".lora_A." in k:
                deltas.setdefault(k.split(".lora_A.")[0], {})["A"] = f.get_tensor(k)
            elif ".lora_B." in k:
                deltas.setdefault(k.split(".lora_B.")[0], {})["B"] = f.get_tensor(k)
    print(f"lora modules: {len(deltas)}", file=sys.stderr)

    # map lora module -> base key (base adds 'model.' in front of 'diffusion_model.')
    patch = {}  # base_key -> (A, B)
    for mod, ab in deltas.items():
        if "A" not in ab or "B" not in ab:
            continue
        bk = f"model.{mod}.weight"
        if bk in base_hdr:
            patch[bk] = (ab["A"], ab["B"])
    print(f"patched tensors: {len(patch)} of {len(base_hdr)-1}", file=sys.stderr)

    # plan output: patched tensors become F8_E4M3, others copied with original dtype
    out_hdr, offsets = {}, {}
    pos = 0
    for k, v in base_hdr.items():
        if k == "__metadata__":
            continue
        dtype = "F8_E4M3" if k in patch else v["dtype"]
        nbytes = v["data_offsets"][1] - v["data_offsets"][0]
        if k in patch:
            shape = v["shape"]
            nbytes = 1
            for d in shape:
                nbytes *= d
        out_hdr[k] = {"dtype": dtype, "shape": v["shape"], "data_offsets": [pos, pos + nbytes]}
        offsets[k] = (pos, nbytes)
        pos += nbytes

    with open(args.out, "wb") as out, open(args.base, "rb") as base_f:
        hdr_json = json.dumps({**out_hdr, "__metadata__": {"format": "pt"}}).encode()
        out.write(struct.pack("<Q", len(hdr_json)))
        out.write(hdr_json)
        for i, (k, (rel_off, nbytes)) in enumerate(offsets.items()):
            if k in patch:
                A, B = patch[k]
                with safe_open(args.base, framework="pt", device="cpu") as f:
                    w = f.get_tensor(k).to(torch.float32)
                w += torch.mm(B.to(torch.float32), A.to(torch.float32)) * args.strength
                t = w.to(torch.float8_e4m3fn).view(torch.uint8).numpy().tobytes()
                assert len(t) == nbytes
                out.write(t)
                if i < 3 or i % 100 == 0:
                    dw = (torch.mm(B.to(torch.float32), A.to(torch.float32)) * args.strength).norm()
                    print(f"  {k}: |dW|={dw:.2f} |W|={w.norm():.1f}", file=sys.stderr)
            else:
                base_f.seek(base_data0 + base_hdr[k]["data_offsets"][0])
                out.write(base_f.read(nbytes))
            if i % 200 == 0:
                print(f"  {i}/{len(offsets)}", file=sys.stderr)
    print(f"done: {args.out} ({Path(args.out).stat().st_size/2**30:.1f} GiB)", file=sys.stderr)


if __name__ == "__main__":
    main()
