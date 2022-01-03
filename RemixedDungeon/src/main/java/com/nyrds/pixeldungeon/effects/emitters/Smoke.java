package com.nyrds.pixeldungeon.effects.emitters;

import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

public class Smoke extends Emitter {

    private int pos;

    private static final Factory factory = new Factory() {

        @Override
        public void emit( Emitter emitter, int index, float x, float y ) {
            SmokeParticle p = (SmokeParticle)emitter.recycle( SmokeParticle.class );
            p.reset( x, y );
        }
    };

    public Smoke(int pos ) {
        super();

        this.pos = pos;

        PointF p = DungeonTilemap.tileCenterToWorld( pos );
        pos( p.x - 4, p.y - 2, 4, 0 );

        pour( factory, 0.2f );
    }

    @Override
    public void update() {
        if (setVisible(Dungeon.isCellVisible(pos))) {
            super.update();
        }
    }

    public static final class SmokeParticle extends PixelParticle {

        public SmokeParticle() {
            super();

            color( 0x000000 );
            speed.set( Random.Float( 8 ), -Random.Float( 8 ) );
        }

        public void reset( float x, float y ) {
            revive();

            this.x = x;
            this.y = y;

            left = lifespan = 2f;
        }

        @Override
        public void update() {
            super.update();
            float p = left / lifespan;
            am = p > 0.8f ? 1 - p : p * 0.25f;
            size( 8 - p * 4 );
        }
    }
}
