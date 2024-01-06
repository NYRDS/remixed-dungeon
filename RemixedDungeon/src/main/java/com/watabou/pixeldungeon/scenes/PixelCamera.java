package com.watabou.pixeldungeon.scenes;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.game.Game;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Visual;
import com.watabou.utils.Point;
import com.watabou.utils.PointF;

class PixelCamera extends Camera {

    private final PointF _scroll = new PointF();

    PixelCamera(float zoom) {
        super(
                (int) (Game.width() - Math.ceil(Game.width() / zoom) * zoom) / 2,
                (int) (Game.height() - Math.ceil(Game.height() / zoom) * zoom) / 2,
                (int) Math.ceil(Game.width() / zoom),
                (int) Math.ceil(Game.height() / zoom),
                zoom);
    }

    public void setTarget(Visual target) {
        if (target == null) {
            scroll.set(_scroll);
        }

        boolean targetChanged = target != this.target;

        super.setTarget(target);

        if (targetChanged) {
            _scroll.set(scroll);
        }
    }

    @Override
    public PointF screenToCamera(int x, int y) {
        if (target == null) {
            _scroll.set(scroll);
        }

        return new PointF(
                (x - this.x) / zoom + _scroll.x,
                (y - this.y) / zoom + _scroll.y);
    }

    @Override
    public Point cameraToScreen(float x, float y) {
        if (target == null) {
            _scroll.set(scroll);
        }

        return new Point(
                (int) ((x - _scroll.x) * zoom + this.x),
                (int) ((y - _scroll.y) * zoom + this.y));
    }

    @Override
    public void update() {
        super.update();

        if (target == null) {
            _scroll.set(scroll);
        }

        if (Math.abs(_scroll.x - scroll.x) > screenWidth * 0.025f) {
            _scroll.x -= 5 * (_scroll.x - scroll.x) * GameLoop.elapsed;
        }

        if (Math.abs(_scroll.y - scroll.y) > screenHeight * 0.025f) {
            _scroll.y -= 5 * (_scroll.y - scroll.y) * GameLoop.elapsed;
        }
    }

    @Override
    protected void updateMatrix() {
        float sx = PixelScene.align(this, _scroll.x + shakeX);
        float sy = PixelScene.align(this, _scroll.y + shakeY);

        matrix[0] = +zoom * invW2;
        matrix[5] = -zoom * invH2;

        matrix[12] = -1 + x * invW2 - sx * matrix[0];
        matrix[13] = +1 - y * invH2 - sy * matrix[5];

    }
}
