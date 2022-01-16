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

public class BloodSink extends Emitter {


    private int pos;
    private float rippleDelay = 0;

    private static final Factory factory = new Factory() {

        @Override
        public void emit( Emitter emitter, int index, float x, float y ) {
            BloodParticle p = (BloodParticle)emitter.recycle( BloodParticle.class );
            p.reset( x, y );
        }
    };

    public BloodSink(int pos ) {
        super();

        this.pos = pos;

        PointF p = DungeonTilemap.tileCenterToWorld( pos );
        pos( p.x - 2, p.y + 1, 4, 0 );

        pour( factory, 0.02f );
    }

    @Override
    public void update() {
        if (setVisible(Dungeon.isNorthWallVisible(pos))) {

            super.update();

            if ((rippleDelay -= GameLoop.elapsed) <= 0) {
                GameScene.ripple(pos + Dungeon.level.getWidth()).y -= DungeonTilemap.SIZE / 2;
                rippleDelay = Random.Float( 0.2f, 0.3f );
            }
        }
    }

    public static final class BloodParticle extends PixelParticle {

        public BloodParticle() {
            super();

            acc.y = 50;
            am = 0.5f;

            color( ColorMath.random( 0xe6e600, 0x9fe05d ) );
            size( 2 );
        }

        public void reset( float x, float y ) {
            revive();

            this.x = x;
            this.y = y;

            speed.set( Random.Float( -2, +2 ), 0 );

            left = lifespan = 0.5f;
        }
    }
}
