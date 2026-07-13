package com.nyrds.pixeldungeon.mobs.common;

import com.nyrds.Packable;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.actors.mobs.Mob;

import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Set;

/**
 * Created by DeadDie on 20.08.2016
 */
public class Deathling extends Mob {

    private static final int HEALTH = 4;
    @Packable
    public boolean firstAct = true;

    public Deathling(){
        hp(ht(HEALTH));

        carcassChance = 0;
        flying = true;

        expForKill = 0;
        maxLvl = 32;

        setUndead(true);
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

        if(firstAct) {
            hp(ht());
            firstAct = false;
        }
    }

    @Override
    public void act() {
        adjustStats();
        super.act();
    }

    @Override
    @NotNull
    public Set<Belongings.Slot> getAvailableEquipmentSlots() {
        return EnumSet.of(Belongings.Slot.ARTIFACT, Belongings.Slot.LEFT_ARTIFACT);
    }
}
