package com.nyrds.pixeldungeon.ai;

import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.CharsList;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.utils.Utils;
import org.jetbrains.annotations.NotNull;

/**
 * "Stay there" order: hold the post cell, scan for enemies, engage only within
 * LEASH steps of the post, return to the post when the fight leaves the leash.
 * Post cell is kept in me.getTarget() (states are shared singletons).
 */
public class Guard extends MobAi implements AiState {

	public static final int LEASH = 2;

	public Guard() { }

	private static int post(@NotNull Char me) {
		final Level level = me.level();
		if (level.cellValid(me.getTarget())) {
			return me.getTarget();
		}
		return me.getPos();
	}

	@Override
	public void act(@NotNull Char me) {

		final Level level = me.level();
		final int post = post(me);

		Char enemy = me.getEnemy();
		if (enemy.invalid() || !enemy.isAlive() || me.friendly(enemy)) {
			enemy = CharsList.DUMMY;
			me.setEnemy(enemy);
		}

		me.enemySeen = enemy.valid() && me.isEnemyInFov();

		if (!enemy.valid()) {
			enemy = chooseEnemy(me, 1.0f);
			me.setEnemy(enemy);
			me.enemySeen = enemy.valid() && me.isEnemyInFov();
		}

		if (me instanceof Mob && ((Mob) me).isHumanoid()
				&& MobItemAi.tryUseItem((Mob) me, MobItemAi.Context.COMBAT)) {
			return;
		}

		// fight what we can hit even if the hero can't see it; otherwise only
		// engage enemies visible to the hero (pets share the hero's FoV)
		if (enemy.valid() && (me.enemySeen || me.canAttack(enemy))
				&& level.distance(me.getPos(), post) <= LEASH
				&& level.distance(enemy.getPos(), post) <= LEASH) {
			if (me.canAttack(enemy)) {
				me.doAttack(enemy);
			} else {
				me.doStepTo(enemy.getPos());
			}
			return;
		}

		if (me.getPos() != post) {
			me.doStepTo(post);
			return;
		}

		me.spend(Char.TICK);
	}

	@Override
	public String status(Char me) {
		return Utils.format(R.string.Mob_StaGuardStatus,
				me.getName());
	}

	@Override
	public void gotDamage(Char me, NamedEntityKind src, int dmg) {
		if (!(src instanceof Char)) { // DoT ticks, gas, traps: keep guarding
			return;
		}

		final Char attacker = (Char) src;
		if (!me.friendly(attacker)
				&& me.level().distance(attacker.getPos(), post(me)) <= LEASH) {
			me.setEnemy(attacker);
		}
	}
}
