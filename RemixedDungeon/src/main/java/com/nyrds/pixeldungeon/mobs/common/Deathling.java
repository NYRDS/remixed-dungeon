package com.nyrds.pixeldungeon.mobs.common;

import com.nyrds.pixeldungeon.mobs.necropolis.UndeadMob;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.utils.Random;

/**
 * Created by DeadDie on 20.08.2016
 */
public class Deathling extends UndeadMob {

    private static final int HEALTH = 4;

    public Deathling(){
        hp(ht(HEALTH + getModifier()));

        baseSpeed = 1.1f;
        baseDefenseSkill = 1 + getModifier();
        flying = true;

        exp = 0;
        maxLvl = 32;
    }

    private int getModifier(){
        Hero hero = Dungeon.hero;
        if (hero != null){
            return hero.lvl() + hero.skillLevel()*hero.skillLevel();
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
