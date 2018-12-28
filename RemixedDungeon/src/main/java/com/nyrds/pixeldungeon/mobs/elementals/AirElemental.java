package com.nyrds.pixeldungeon.mobs.elementals;

import com.nyrds.pixeldungeon.mechanics.spells.WindGust;
import com.nyrds.pixeldungeon.mobs.common.IDepthAdjustable;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Bleeding;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.potions.PotionOfLevitation;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.utils.Random;

import androidx.annotation.NonNull;

public class AirElemental extends Mob implements IDepthAdjustable {


	private static WindGust windGust = new WindGust();

	public AirElemental() {

		adjustStats(Dungeon.depth);

		flying = true;
		
		loot = new PotionOfLevitation();
		lootChance = 0.1f;

		IMMUNITIES.add(Bleeding.class);
	}

	@Override
	public int magicLvl() {
		return Math.max(Math.min(7, exp / 2),3);
	}

	public void adjustStats(int depth) {
		hp(ht(depth * 3 + 1));
		defenseSkill = depth * 2 + 1;
		exp = depth + 1;
		maxLvl = depth + 2;
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange(0, ht() / 4);
	}

	@Override
	public int attackSkill(Char target) {
		return defenseSkill * 2;
	}

	@Override
	public int dr() {
		return exp / 5;
	}

	@Override
	public boolean getCloser(int target) {
		if (getState() == HUNTING && Dungeon.level.distance(getPos(), target) < magicLvl() - 1) {
			return getFurther(target);
		}

		return super.getCloser(target);
	}

	@Override
    public boolean canAttack(Char enemy) {

		if (Dungeon.level.adjacent(getPos(), enemy.getPos())) {
			return false;
		}

		Ballistica.cast(getPos(), enemy.getPos(), true, false);

		for (int i = 1; i < magicLvl(); i++) {
			if (Ballistica.trace[i] == enemy.getPos()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean zap(@NonNull Char enemy) {
		windGust.cast(this, enemy.getPos());
		super.zap(enemy);
		return true;
	}

}
