package com.nyrds.pixeldungeon.mobs.common;

import com.nyrds.pixeldungeon.mobs.necropolis.UndeadMob;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.ParalyticGas;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Blindness;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Charm;
import com.watabou.pixeldungeon.actors.buffs.FlavourBuff;
import com.watabou.pixeldungeon.actors.buffs.Roots;
import com.watabou.pixeldungeon.actors.buffs.Slow;
import com.watabou.pixeldungeon.actors.buffs.Vertigo;
import com.watabou.pixeldungeon.actors.buffs.Weakness;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfPsionicBlast;
import com.watabou.utils.Random;

/**
 * Created by DeadDie on 20.08.2016
 */
public class Deathling extends UndeadMob {

    private static final int HEALTH = 4;

    public Deathling(){
        hp(ht(HEALTH + getModifier()));

        baseSpeed = 1.1f;
        defenseSkill = 1 + getModifier();
        flying = true;

        EXP = 0;
        maxLvl = 32;
    }

    private int getModifier(){
        if (Dungeon.hero != null){
            return Dungeon.hero.lvl();
        }
        return 1;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(1, 4 + getModifier());
    }

    @Override
    public int attackSkill( Char target ) {
        return 4 + getModifier();
    }

    @Override
    public int dr() {
        return 1 + getModifier();
    }
}
