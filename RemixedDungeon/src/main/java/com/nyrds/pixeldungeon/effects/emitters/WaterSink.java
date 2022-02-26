package com.nyrds.pixeldungeon.effects.emitters;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.utils.ColorMath;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

public class WaterSink extends Emitter {

    private final int pos;
    private float rippleDelay = 0;

    private static final Factory factory = new Factory() {

        @Override
        public void emit(Emitter emitter, int index, float x, float y) {
            WaterParticle p = (WaterParticle) emitter.recycle(WaterParticle.class);
            p.reset(x, y);
        }
    };

    public WaterSink(int pos) {
        super();

        this.pos = pos;

        PointF p = DungeonTilemap.tileCenterToWorld(pos);
        pos(p.x - 2, p.y + 1, 4, 0);

        pour(factory, 0.05f);
    }

    @Override
    public void update() {
        if (setVisible(Dungeon.isNorthWallVisible(pos))) {

            super.update();

            if ((rippleDelay -= GameLoop.elapsed) <= 0) {
                GameScene.ripple(pos + Dungeon.level.getWidth()).setY(GameScene.ripple(pos + Dungeon.level.getWidth()).getY() - DungeonTilemap.SIZE / 2);
                rippleDelay = Random.Float(0.2f, 0.3f);
            }
        }
    }

    public static final class WaterParticle extends PixelParticle {

        public WaterParticle() {
            super();

            acc.y = 50;
            am = 0.5f;

            color(ColorMath.random(0xb6ccc2, 0x3b6653));
            size(2);
        }

        public void reset(float x, float y) {
            revive();

            this.setX(x);
            this.setY(y);

            speed.set(Random.Float(-2, +2), 0);

            left = lifespan = 0.5f;
        }
    }
}
