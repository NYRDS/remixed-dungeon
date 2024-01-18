package com.nyrds.pixeldungeon.mobs.guts;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Amok;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.buffs.Sleep;
import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.potions.PotionOfHealing;

import org.jetbrains.annotations.NotNull;

/**
 * Created by DeadDie on 12.02.2016
 */
public class YogsHeart extends Mob {
    {
        hp(ht(450));
        baseDefenseSkill = 40;
        baseAttackSkill  = 26;
        dmgMin = 35;
        dmgMax = 45;
        dr = 22;

        expForKill = 12;

        addImmunity(ToxicGas.class);
        addImmunity(Paralysis.class);
        addImmunity(Amok.class);
        addImmunity(Sleep.class);
        addImmunity(Terror.class);
        addImmunity(Burning.class);
    }

    @Override
    public void damage(int dmg, @NotNull NamedEntityKind src) {
        for (Mob mob : level().mobs) {
            mob.beckon(getPos());
        }

        super.damage(dmg, src);
    }


    @Override
    public int defenseProc(Char enemy, int damage) {
        CharUtils.spawnOnNextCell(this, "Larva", (int) (10 * GameLoop.getDifficultyFactor()));

        return super.defenseProc(enemy, damage);
    }

	@Override
	public boolean act() {

		Mob mob = level().getRandomMob();

		if(mob!=null && mob.isAlive() && !mob.isPet()) {
			PotionOfHealing.heal(mob,0.2f);
		}

		return super.act();
	}

    @Override
    public boolean canBePet() {
        return false;
    }
}
