package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.platform.input.Touchscreen;
import com.watabou.noosa.TouchArea;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.PointF;

import java.util.ArrayList;
import java.util.List;

public class WndAlchemy extends Window {

    TouchController touch;
    List<ItemSprite> sprites = new ArrayList<>();

    public WndAlchemy() {

        ItemSprite sword = new ItemSprite(ItemFactory.itemByName("Sword"));
        sword.setPos(20, 20);
        sprites.add(sword);
        add(sword);

        ItemSprite woodenShield = new ItemSprite(ItemFactory.itemByName("WoodenShield"));
        woodenShield.setPos(60, 20);
        sprites.add(woodenShield);
        add(woodenShield);

        touch = new TouchController();
        touch.camera = PixelScene.uiCamera;
        add(touch);

        resize(160, 120);
    }

    public class TouchController extends TouchArea {

        private final float dragThreshold;
        private boolean touchStartedOnSprite = false;
        private boolean dragging = false;
        private ItemSprite draggedSprite;
        private final PointF lastPos = new PointF();

        public TouchController() {
            super(WndAlchemy.this.chrome);
            dragThreshold = PixelScene.defaultZoom * 8;
        }

        @Override
        protected void onTouchDown(Touchscreen.Touch touch) {
            touchStartedOnSprite = false;
            for (int i = sprites.size() - 1; i >= 0; i--) {
                ItemSprite sprite = sprites.get(i);
                if (sprite.overlapsScreenPoint((int) touch.current.x, (int) touch.current.y)) {
                    touchStartedOnSprite = true;
                    draggedSprite = sprite;
                    break;
                }
            }
            GLog.debug("Touch: %3.0f %3.0f", touch.current.x, touch.current.y);
            super.onTouchDown(touch);
        }

        @Override
        protected void onDrag(Touchscreen.Touch t) {
            if (dragging) {
                PointF scrollStep = PointF.diff(lastPos, t.current);
                float zoom = camera.zoom;

                scrollStep.x /= zoom;
                scrollStep.y /= zoom;

                if (draggedSprite != null) {
                    draggedSprite.x -= scrollStep.x;
                    draggedSprite.y -= scrollStep.y;
                    draggedSprite.dirtyMatrix = true;
                }

                GLog.debug("scroll this: %3.0f : %3.0f", scrollStep.x, scrollStep.y);
            } else if (PointF.distance(t.current, t.start) > dragThreshold) {
                if (touchStartedOnSprite) {
                    dragging = true;
                }
            }
            lastPos.set(t.current);
        }

        @Override
        protected void onTouchUp(Touchscreen.Touch touch) {
            touchStartedOnSprite = false;
            dragging = false;
            draggedSprite = null;
            GLog.debug("Touch: %3.0f %3.0f", touch.current.x, touch.current.y);
            super.onTouchUp(touch);
        }
    }
}