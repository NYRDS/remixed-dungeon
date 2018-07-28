package com.watabou.pixeldungeon.items.rings;

import com.watabou.pixeldungeon.actors.buffs.Buff;

/**
 * Created by mike on 25.03.2018.
 * This file is part of Remixed Pixel Dungeon.
 */
public class ArtifactBuff extends Buff {

    private Artifact source;

    public void setSource(Artifact source) {
        this.source = source;
    }


    @Override
    public boolean act() {
        if(source==null) { // non-artifact source
            detach();
        }
        return super.act();
    }

    @Override
    public boolean dontPack() {
        return source != null;
    }


}
