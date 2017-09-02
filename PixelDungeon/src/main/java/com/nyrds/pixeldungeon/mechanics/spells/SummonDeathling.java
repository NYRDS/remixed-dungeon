package com.nyrds.pixeldungeon.mechanics.spells;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.common.Deathling;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.mobs.Mob;

public class SummonDeathling extends SummoningSpell {

    public SummonDeathling(){
        targetingType = SpellHelper.TARGET_NONE;
        magicAffinity = SpellHelper.AFFINITIES[0];
        name          = Game.getVar(R.string.Necromancy_SummonDeathlingName);
        desc          = Game.getVar(R.string.SummonDeathling_Info);
        imageIndex = 0;
    }

    @Override
    public Mob getSummonMob(){
        return new Deathling();
    }

    public int spellCost(){
        return 5;
    }

    @Override
    public String texture(){
        return "spellsIcons/necromancy.png";
    }

    @Override
    public int textureResolution(){
        return 32;
    }
}
