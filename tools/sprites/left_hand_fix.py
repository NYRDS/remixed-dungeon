import os
from PIL import Image

xs = 64
ys = 64


rootDir = '../../RemixedDungeon/src/main/assets/hero_modern'


def get_frame(img, fn):
    x = xs * fn
    y = 0
    return img.crop((x, y, x + xs, y + ys))


def put_frame(img, frame, fn, dx, dy):
    x = xs * fn
    y = 0

    img.paste(frame, (x+dx, y+dy))


def clone_frame(img, src, dst):
    put_frame(img, get_frame(img, src), dst, 0,0)


def left_hand_fix(fname, dir_name):
    if '.png' in fname and 'left' in fname:
        print('\t%s' % fname)

        full_path = dir_name + "/" + fname
        img = Image.open(full_path)

        clone_frame(img, 0, 19)

        img.save(full_path)


for dirName, subdirList, fileList in os.walk(rootDir):
    print('Found directory: %s' % dirName)

    for fname in fileList:

        if 'items' in dirName or 'hands' in dirName:
            xs = 32
            ys = 32
            left_hand_fix(fname, dirName)
