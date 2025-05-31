package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.platform.input.Touchscreen;
import com.watabou.noosa.TouchArea;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.PointF;

public class WndAlchemy extends Window {

    TouchController touch;

    ItemSprite sprite;

    public WndAlchemy() {
        sprite = new ItemSprite(ItemFactory.itemByName("Sword"));
        sprite.setPos(20, 20);
        add(sprite);

        touch = new TouchController();
        touch.camera = PixelScene.uiCamera;

        add(touch);


        resize(160, 120);
    }

    public class TouchController extends TouchArea {

        private final float dragThreshold;

        public TouchController() {
            super(WndAlchemy.this.chrome);
            dragThreshold = PixelScene.defaultZoom * 8;
        }

        @Override
        protected void onClick(Touchscreen.Touch touch) {
            GLog.debug("click %3.0f %3.0f %b", x,y, dragging);
            if (dragging) {
                dragging = false;
            }
        }

        @Override
        protected void onTouchDown(Touchscreen.Touch touch) {
            GLog.debug("Touch: %3.0f %3.0f", touch.current.x, touch.current.y);
            super.onTouchDown(touch);
        }

        @Override
        protected void onTouchUp(Touchscreen.Touch touch) {
            GLog.debug("Touch: %3.0f %3.0f", touch.current.x, touch.current.y);
            super.onTouchUp(touch);
        }

        // true if dragging is in progress
        private boolean dragging = false;
        // last touch cords
        private final PointF lastPos = new PointF();

        @Override
        protected void onDrag(Touchscreen.Touch t) {
            if (dragging) {

                if (sprite.overlapsScreenPoint((int) t.current.x, (int) t.current.y)){
                    PointF scrollStep = PointF.diff(lastPos, t.current);
                    sprite.x -= scrollStep.x;
                    sprite.y -= scrollStep.y;
                    sprite.dirtyMatrix = true;

                    GLog.debug("scroll this: %3.0f : %3.0f", scrollStep.x, scrollStep.y);                }

            } else if (PointF.distance(t.current, t.start) > dragThreshold) {

                dragging = true;

            }
            lastPos.set(t.current);
        }
    }
}

