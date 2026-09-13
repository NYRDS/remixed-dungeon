#!/usr/bin/env python3
"""Pixel-art postprocess: per-cell color voting instead of single-pixel sampling.

Methods:
  nearest — old behavior: one pixel sampled per grid cell (noisy on painterly art)
  median  — per-cell median color (edges survive, no invented blend colors)
  mode    — per-cell dominant color after coarse pre-quantization (crispest fills)

Palette: MAXCOVERAGE usually beats MEDIANCUT on pixel art (keeps saturated accents).

Usage: pixelize.py IN.png OUT.png [--pixel 6] [--colors 128] [--method median] [--scale-back 6]
"""
import argparse
import numpy as np
from PIL import Image


def cell_reduce(arr, gh, gw, method):
    h, w, _ = arr.shape
    ch, cw = h // gh, w // gw
    arr = arr[: gh * ch, : gw * cw]
    cells = arr.reshape(gh, ch, gw, cw, 3)
    if method == "median":
        return np.median(cells, axis=(1, 3)).astype(np.uint8)
    if method == "mode":
        # dominant color per cell: quantize source coarsely, then vote
        q = Image.fromarray(arr).quantize(colors=64, method=Image.MAXCOVERAGE)
        idx = np.asarray(q).reshape(gh, ch, gw, cw)
        out = np.zeros((gh, gw), dtype=np.int64)
        for i in range(gh):
            for j in range(gw):
                out[i, j] = np.bincount(idx[i, :, j, :].ravel(), minlength=64).argmax()
        pal = np.asarray(q.getpalette(), dtype=np.uint8).reshape(-1, 3)
        return pal[out]
    raise ValueError(method)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("infile")
    ap.add_argument("outfile")
    ap.add_argument("--pixel", type=int, default=6)
    ap.add_argument("--colors", type=int, default=128)
    ap.add_argument("--method", default="median", choices=["nearest", "median", "mode"])
    ap.add_argument("--quantizer", default="MAXCOVERAGE", choices=["MAXCOVERAGE", "MEDIANCUT"])
    args = ap.parse_args()

    im = Image.open(args.infile).convert("RGB")
    w, h = im.size
    gh, gw = h // args.pixel, w // args.pixel
    arr = np.asarray(im)

    if args.method == "nearest":
        small = im.resize((gw, gh), Image.NEAREST)
    else:
        small = Image.fromarray(cell_reduce(arr, gh, gw, args.method))

    small = small.quantize(colors=args.colors, method=getattr(Image, args.quantizer),
                           dither=Image.NONE).convert("RGB")
    small.save(args.outfile.replace(".png", "_true.png"))
    small.resize((gw * args.pixel, gh * args.pixel), Image.NEAREST).save(args.outfile)
    print(args.outfile, small.size)


if __name__ == "__main__":
    main()
