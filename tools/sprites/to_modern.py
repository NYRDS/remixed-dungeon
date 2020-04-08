import os
from PIL import Image

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


rootDir = '../../RemixedDungeon/src/main/assets/hero'

out = Image.new('RGBA', (oXs, oYs))

for dirName, subdirList, fileList in os.walk(rootDir):

    print('Found directory: %s' % dirName)
    for fname in fileList:
        if '.png' in fname:
            print('\t%s' % fname)
            fullPath = dirName + "/" + fname
            resize(fullPath,fullPath)
