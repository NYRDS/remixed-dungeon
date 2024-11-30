
package com.watabou.pixeldungeon.actors.mobs;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Charm;
import com.watabou.pixeldungeon.actors.buffs.Sleep;
import com.watabou.pixeldungeon.items.quest.DriedRose;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfLullaby;
import com.watabou.pixeldungeon.items.weapon.enchantments.Leech;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class Succubus extends Mob {

	private static final int BLINK_DELAY = 5;

	private int delay = 0;

	public Succubus() {

		hp(ht(80));
		baseDefenseSkill = 25;
		baseAttackSkill  = 40;
		dmgMin = 15;
		dmgMax = 25;
		dr = 10;

		expForKill = 12;
		maxLvl = 25;
		carcassChance = 0;

		loot(new ScrollOfLullaby(), 0.05f);

		addResistance(Leech.class);
		addImmunity(Sleep.class);
	}

	@Override
	public void onSpawn(Level level) {
		super.onSpawn(level);
		setViewDistance(level.getViewDistance() + 1);
	}

	@Override
	public int attackProc(@NotNull Char enemy, int damage) {

		if (Random.Int(3) == 0) {
			Char target = enemy;

			if (enemy.hasBuff(DriedRose.OneWayLoveBuff.class)) {
				target = this;
			}

			float duration = Charm.durationFactor(target) * Random.IntRange(2, 5);

			Buff.affect(target, Charm.class, duration);
		}

		return damage;
	}

	@Override
    public boolean getCloser(int target,  boolean ignorePets) {
		if (level().fieldOfView[target] && level().distance(getPos(), target) > 2 && delay <= 0) {
			CharUtils.blinkTo(this, target);
			delay = BLINK_DELAY;
			spend(-1 / speed());
			return true;
		} else {
			delay--;
			return super.getCloser(target, ignorePets);
		}
	}
}
