package com.watabou.pixeldungeon.items.rings;

import com.nyrds.pixeldungeon.utils.EntityIdSource;
import com.watabou.pixeldungeon.actors.buffs.Buff;

/**
 * Created by mike on 25.03.2018.
 * This file is part of Remixed Pixel Dungeon.
 */
public class ArtifactBuff extends Buff {

    @Override
    public boolean act() {
        if(getSource()== EntityIdSource.INVALID_ID) { // non-artifact source
            detach();
        }
        return super.act();
    }

    @Override
    public boolean dontPack() {
        return getSource() != EntityIdSource.INVALID_ID;
    }
}
