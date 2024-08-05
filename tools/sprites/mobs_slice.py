import json
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



rootDir = '../../RemixedDungeon/src/main/assets/spritesDesc'
spriteDir = '../../RemixedDungeon/src/main/assets/'


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

            img = Image.open(f"{spriteDir}{spriteDesc['texture']}")
            fw = spriteDesc["width"]
            fh = spriteDesc["height"]
            mobName = fname.replace('.json', '')
            os.makedirs(f"./sprites/{mobName}/", exist_ok=True)

            animations = ["idle", "run", "attack", "die"]

            allFrames = set()

            for animation in animations:
                if animation in spriteDesc:
                    if 'frames' in spriteDesc[animation]:
                        for frame in spriteDesc[animation]['frames']:
                            allFrames.add(frame)

            for i in allFrames:
                x = fw * i
                y = 0

                if x + fw >= img.width:
                    x -= int(img.width/fw) * fw
                    y += fh

                frame = img.crop((x, y, x + fw, y + fh))
                nx = nXs * i
                ny = 0
                out = Image.new('RGBA', (fw, fh))
                out.paste(frame, (0,0))
                out.save(f"./sprites/{mobName}/{i}.png")

            print(spriteDesc)

