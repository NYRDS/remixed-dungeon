package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.input.Touchscreen;
import com.nyrds.util.Util;
import com.watabou.noosa.TouchArea;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.List;

public class WndAlchemy extends Window {

    TouchController touch;
    List<ItemSprite> sprites = new ArrayList<>();

    float timer = 0;
    private static final float REPULSION_FORCE = 1.5f;
    private static final float BORDER_MARGIN = 5f; // Margin to keep items within window

    public WndAlchemy() {
        int windowWidth = 160;
        int windowHeight = 120;

        for (Item item : Dungeon.hero.getBelongings()) {
            ItemSprite sprite = new ItemSprite(item);

            // Calculate random position within window boundaries
            float maxX = windowWidth - sprite.width() - BORDER_MARGIN;
            float maxY = windowHeight - sprite.height() - BORDER_MARGIN;

            // Ensure random position stays within bounds
            float randomX = Math.max(BORDER_MARGIN, Random.Float(maxX));
            float randomY = Math.max(BORDER_MARGIN, Random.Float(maxY));

            sprite.setPos(randomX, randomY);
            sprites.add(sprite);
            add(sprite);
        }

        touch = new TouchController();
        touch.camera = PixelScene.uiCamera;
        add(touch);

        resize(windowWidth, windowHeight);
    }

    @Override
    public void update() {
        super.update();
        timer += GameLoop.elapsed;

        // Apply repulsive forces between sprites and enforce boundaries
        for (int i = 0; i < sprites.size(); i++) {
            ItemSprite sprite = sprites.get(i);

            // Only apply physics to non-dragged sprites
            if (sprite != touch.draggedSprite) {
                // Apply repulsion with other sprites
                for (int j = 0; j < sprites.size(); j++) {
                    if (i != j) {
                        ItemSprite other = sprites.get(j);
                        applyRepulsion(sprite, other);
                    }
                }

                // Keep sprite within window boundaries
                enforceBoundaries(sprite);
            }
        }
    }

    // Keep sprite within window boundaries
    private void enforceBoundaries(ItemSprite sprite) {
        float minX = BORDER_MARGIN;
        float minY = BORDER_MARGIN;
        float maxX = width - sprite.width() - BORDER_MARGIN;
        float maxY = height - sprite.height() - BORDER_MARGIN;

        float newX = Util.clamp(sprite.getX(), minX, maxX);
        float newY = Util.clamp(sprite.getY(), minX, maxY);

        if (sprite.getX() != newX || sprite.getY() != newY) {
            sprite.setPos(newX, newY);
        }
    }

    private void applyRepulsion(ItemSprite s1, ItemSprite s2) {
        // Skip if either sprite is being dragged
        if (s1 == touch.draggedSprite || s2 == touch.draggedSprite) {
            return;
        }

        // Calculate distance between sprite centers
        float dx = s2.center().x - s1.center().x;
        float dy = s2.center().y - s1.center().y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        // Avoid division by zero
        if (distance == 0) {
            return;
        }

        // Calculate minimum distance where sprites don't overlap
        float minDistance = (s1.width() + s2.width()) / 2 * 0.8f;

        // If sprites are overlapping
        if (distance < minDistance) {
            // Calculate overlap amount
            float overlap = minDistance - distance;

            // Normalize direction vector
            dx /= distance;
            dy /= distance;

            // Apply repulsion proportional to overlap
            float force = overlap * REPULSION_FORCE * GameLoop.elapsed;

            // Move sprites apart
            s1.setPos(s1.getX() - dx * force, s1.getY() - dy * force);
            s2.setPos(s2.getX() + dx * force, s2.getY() + dy * force);
        }
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
                    // Calculate new position
                    float newX = draggedSprite.getX() - scrollStep.x;
                    float newY = draggedSprite.getY() - scrollStep.y;
                    draggedSprite.setPos(newX, newY);

                    enforceBoundaries(draggedSprite);
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

            // Clear dragged sprite after release
            draggedSprite = null;

            GLog.debug("Touch: %3.0f %3.0f", touch.current.x, touch.current.y);
            super.onTouchUp(touch);
        }
    }
}