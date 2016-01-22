package com.nyrds.pixeldungeon.mobs.elementals;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.potions.PotionOfLevitation;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.utils.Random;

public class AirElemental extends Mob {

	private static final int maxDistance = 3;

	public AirElemental() {

		adjustLevel(Dungeon.depth);

		flying = true;
		
		loot = new PotionOfLevitation();
		lootChance = 0.1f;
	}

	private void adjustLevel(int depth) {
		hp(ht(depth * 3 + 1));
		defenseSkill = depth * 2 + 1;
		EXP = depth + 1;
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
		return EXP / 5;
	}

	@Override
	protected boolean getCloser(int target) {
		if (state == HUNTING && Dungeon.level.distance(getPos(), target) < maxDistance - 1) {
			return getFurther(target);
		}

		return super.getCloser(target);
	}

	@Override
	protected boolean canAttack(Char enemy) {

		if (Dungeon.level.adjacent(getPos(), enemy.getPos())) {
			return false;
		}

		Ballistica.cast(getPos(), enemy.getPos(), true, false);

		for (int i = 1; i < maxDistance; i++) {
			if (Ballistica.trace[i] == enemy.getPos()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int attackProc(Char enemy, int damage) {

		Ballistica.cast(getPos(), enemy.getPos(), true, false);

		Char ch;

		for (int i = 1; i < maxDistance; i++) {

			int c = Ballistica.trace[i];

			if ((ch = Actor.findChar(c)) != null && ch instanceof Hero) {
				int next = Ballistica.trace[i + 1];
				if ((Dungeon.level.passable[next] || Dungeon.level.avoid[next])
						&& Actor.findChar(next) == null) {
					ch.move(next);
					ch.getSprite().move(ch.getPos(), next);
					Dungeon.observe();
				} else {
					return damage * 2;
				}
			}
		}
		return damage;
	}
}
