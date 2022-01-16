package com.nyrds.pixeldungeon.effects.emitters;

import com.watabou.noosa.particles.Emitter;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.effects.Halo;
import com.watabou.pixeldungeon.effects.particles.FlameParticle;
import com.watabou.utils.PointF;

public class Torch extends Emitter {

    private int pos;

    public Torch(int pos ) {
        super();

        this.pos = pos;

        PointF p = DungeonTilemap.tileCenterToWorld( pos );
        pos( p.x - 1, p.y + 3, 2, 0 );

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
