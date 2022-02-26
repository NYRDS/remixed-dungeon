package com.nyrds.pixeldungeon.effects.emitters;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.watabou.noosa.Group;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

public class IceVein extends Group {

    private int pos;

    private float delay;

    public IceVein(int pos ) {
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
                ((IceSparkle)recycle( IceSparkle.class )).reset(
                    p.x + Random.Float( DungeonTilemap.SIZE ),
                    p.y + Random.Float( DungeonTilemap.SIZE ) );
            }
        }
    }

    public static final class IceSparkle extends PixelParticle {

        public void reset( float x, float y ) {
            revive();

            this.setX(x);
            this.setY(y);

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
