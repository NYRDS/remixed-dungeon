package com.watabou.pixeldungeon.actors.mobs;

import java.util.HashSet;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Freezing;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.actors.buffs.Frost;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.effects.Pushing;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.potions.PotionOfFrost;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfPsionicBlast;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.AirElementalSprite;
import com.watabou.utils.Random;

public class AirElemental extends Mob {

	public AirElemental() {
		spriteClass = AirElementalSprite.class;

		adjustLevel(Dungeon.depth);

		loot = new PotionOfFrost();
		lootChance = 0.1f;
	}

	private void adjustLevel(int depth) {
		hp(ht(depth * 3));
		defenseSkill = depth * 2;
		EXP = depth;
		maxLvl = depth + 2;

	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange(0, ht() / 4);
	}

	@Override
	public int attackSkill(Char target) {
		return defenseSkill * 2 + 10;
	}

	@Override
	public int dr() {
		return 5;
	}

	@Override
	protected boolean getCloser( int target ) {
		if (state == HUNTING) {
			return enemySeen && getFurther( target );
		} else {
			return super.getCloser( target );
		}
	}
	
	@Override
	protected boolean canAttack(Char enemy) {
		return !Level.adjacent(pos, enemy.pos)
				&& Ballistica.cast(pos, enemy.pos, false, true) == enemy.pos;
	}

	@Override
	public int attackProc(Char enemy, int damage) {

		int maxDistance = 7;
		Ballistica.distance = Math.min(Ballistica.distance, maxDistance);

		Char ch;

		for (int i = 1; i < Ballistica.distance; i++) {

			int c = Ballistica.trace[i];

			if ((ch = Actor.findChar(c)) != null && ch instanceof Hero) {
				int next = Ballistica.trace[i + 1];
				if ((Level.passable[next] || Level.avoid[next])
						&& Actor.findChar(next) == null) {
					ch.move(next);
					ch.getSprite().place(next);
				}
			}
		}
		return damage;
	}
}
