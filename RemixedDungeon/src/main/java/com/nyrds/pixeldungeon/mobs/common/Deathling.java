package com.nyrds.pixeldungeon.mobs.common;

import com.nyrds.pixeldungeon.mobs.necropolis.UndeadMob;
import com.watabou.pixeldungeon.actors.Char;

/**
 * Created by DeadDie on 20.08.2016
 */
public class Deathling extends UndeadMob {

    private static final int HEALTH = 4;

    public Deathling(){
        hp(ht(HEALTH));

        flying = true;

        exp = 0;
        maxLvl = 32;
        setSkillLevel(3);
    }

    void adjustStats() {
        Char hero = getOwner();

        int modifier =  hero.lvl() + hero.skillLevel()*hero.skillLevel();

        baseSpeed = 1.1f;
        baseDefenseSkill = 1 + modifier;
        baseAttackSkill  = 4 + modifier;
        dmgMax = 4 + modifier;
        dmgMin = 1;
        dr  = modifier;
        ht(HEALTH + modifier);
    }

    @Override
    public boolean act() {
        adjustStats();
        return super.act();
    }
}
