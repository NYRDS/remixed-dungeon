package com.nyrds.retrodungeon.mobs.elementals;

import android.support.annotation.NonNull;

import com.nyrds.retrodungeon.mobs.common.IDepthAdjustable;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.particles.WindParticle;
import com.watabou.pixeldungeon.items.potions.PotionOfLevitation;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.utils.Random;

public class AirElemental extends Mob implements IDepthAdjustable {

	private static final int maxDistance = 3;

	public AirElemental() {

		adjustStats(Dungeon.depth);

		flying = true;
		
		loot = new PotionOfLevitation();
		lootChance = 0.1f;
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
	protected boolean getCloser(int target) {
		if (getState() == HUNTING && Dungeon.level.distance(getPos(), target) < maxDistance - 1) {
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
	public int attackProc(@NonNull Char enemy, int damage) {

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

					ch.getSprite().emitter().burst( WindParticle.FACTORY, 5 );
					ch.getSprite().burst( 0xFF99FFFF, 3 );
					Sample.INSTANCE.play( Assets.SND_MELD );
				} else {
					return damage * 2;
				}
			}
		}
		return damage;
	}

	@Override
	public boolean zap(@NonNull Char enemy) {
		attackProc(enemy, damageRoll());
		super.zap(enemy);
		return true;
	}

}
