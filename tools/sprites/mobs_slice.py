import os
from PIL import Image
import json

xs = 14
ys = 17


nXs = 32
nYs = 32

xPad = int((nXs - xs) / 2)
yPad = int((nYs - ys) / 2)

nFrames = 19

oXs = 1024
oYs = 32


def resize(iname, oname):
    img = Image.open(iname)

    for i in range(nFrames):
        x = xs * i
        y = 0

        frame = img.crop((x, y, x + xs, y + ys))

        nx = nXs * i
        ny = 0
        out.paste(frame, (nx + xPad, ny + yPad))

        #frame.save("out" + str(i) + ".png")

    out.save(oname)


#resize("man.png", "out.png")



rootDir = '../../RemixedDungeon/src/main/assets/spritesDesc'

out = Image.new('RGBA', (oXs, oYs))

def readJson(fname):
    with open(fname) as json_file:
        data = json.load(json_file)
        return data

animations = ["idle", "run", "attack", "die"]

for dirName, subdirList, fileList in os.walk(rootDir):
    print('Found directory: %s' % dirName)
    for fname in fileList:
        if '.json' in fname:
            print('\t%s' % fname)
            fullPath = dirName + "/" + fname
            print(fullPath)
            spriteDesc = readJson(fullPath)


            print(spriteDesc)

