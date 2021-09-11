package com.nyrds.pixeldungeon.effects.emitters;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.watabou.noosa.Group;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

public class Vein extends Group {

    private int pos;

    private float delay;

    public Vein(int pos ) {
        super();

        this.pos = pos;

        delay = Random.Float( 2 );
    }

    @Override
    public void update() {

        if (setVisible(Dungeon.isCellVisible(pos))) {

            super.update();

            if ((delay -= GameLoop.elapsed) <= 0) {

                delay = Random.Float();

                PointF p = DungeonTilemap.tileToWorld( pos );
                ((Sparkle)recycle( Sparkle.class )).reset(
                    p.x + Random.Float( DungeonTilemap.SIZE ),
                    p.y + Random.Float( DungeonTilemap.SIZE ) );
            }
        }
    }

    public static final class Sparkle extends PixelParticle {

        public void reset( float x, float y ) {
            revive();

            this.x = x;
            this.y = y;

            left = lifespan = 0.5f;
        }

        @Override
        public void update() {
            super.update();

            float p = left / lifespan;
            size( (am = p < 0.5f ? p * 2 : (1 - p) * 2) * 2 );
        }
    }
}
