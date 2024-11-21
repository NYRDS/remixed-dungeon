package com.nyrds.pixeldungeon.effects.emitters;

import com.watabou.noosa.particles.Emitter;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.effects.Halo;
import com.watabou.pixeldungeon.effects.particles.FlameParticle;
import com.nyrds.util.PointF;

public class Candle extends Emitter {

    private final int pos;

    public Candle(int pos ) {
        super();

        this.pos = pos;

        PointF p = DungeonTilemap.tileCenterToWorld( pos );
        pos( p.x - 1, p.y - 3, 2, 0 );

        pour( FlameParticle.FACTORY, 0.15f );

        add( new Halo( 16, 0xFFFFCC, 0.2f ).point( p.x, p.y ) );
    }

    @Override
    public void update() {
        if (setVisible(Dungeon.isNorthWallVisible(pos))) {
            super.update();
        }
    }
}
