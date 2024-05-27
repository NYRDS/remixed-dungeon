import cv2

src = cv2.imread('/home/mike/StudioProjects/remixed-dungeon_fix/RemixedDungeon/src/main/assets/ui/avatars.png', cv2.IMREAD_UNCHANGED)

gray = cv2.cvtColor(src, cv2.COLOR_BGR2GRAY)
_, thresh = cv2.threshold(gray, 0, 255, cv2.THRESH_BINARY)

contours, _ = cv2.findContours(thresh, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

for i, cnt in enumerate(contours):
    x, y, w, h = cv2.boundingRect(cnt)
    sprite = src[y:y+h, x:x+w]
    cv2.imwrite(f'sprite_{i}.png', sprite)