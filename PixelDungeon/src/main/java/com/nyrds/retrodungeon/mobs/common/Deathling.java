package com.nyrds.retrodungeon.mobs.common;

import com.nyrds.retrodungeon.mobs.necropolis.UndeadMob;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
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

        exp = 0;
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
