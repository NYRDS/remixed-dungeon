/*
 * Pixel Dungeon
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.watabou.pixeldungeon.actors.mobs;

import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.ResultDescriptions;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Weakness;
import com.watabou.pixeldungeon.effects.MagicMissile;
import com.watabou.pixeldungeon.items.Generator;
import com.watabou.pixeldungeon.items.wands.WandOfBlink;
import com.watabou.pixeldungeon.items.weapon.enchantments.Death;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.sprites.WarlockSprite;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

public class Warlock extends Mob implements Callback {

	private static final float TIME_TO_ZAP = 1f;

	private static final String TXT_SHADOWBOLT_KILLED = Game
			.getVar(R.string.Warlock_Killed);

	public Warlock() {
		spriteClass = WarlockSprite.class;

		hp(ht(70));
		defenseSkill = 18;

		EXP = 11;
		maxLvl = 21;

		loot = Generator.Category.POTION;
		lootChance = 0.83f;

		RESISTANCES.add(Death.class);
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange(12, 20);
	}

	@Override
	public int attackSkill(Char target) {
		return 25;
	}

	@Override
	public int dr() {
		return 8;
	}

	protected void fx( int cell, Callback callback ) {
		if(getSprite().getParent()==null) {
			EventCollector.logException(new Exception("null parent"));
			return;
		}
		MagicMissile.whiteLight( getSprite().getParent(), getPos(), cell, callback );
		Sample.INSTANCE.play( Assets.SND_ZAP );
		getSprite().setVisible(false);
	}
	
	private void blink(int epos) {

		int cell = getPos();

		Ballistica.cast(epos, cell, true, false);

		for (int i = 1; i < 4; i++) {
			int next = Ballistica.trace[i + 1];
			if (Dungeon.level.cellValid(next) && (Dungeon.level.passable[next] || Dungeon.level.avoid[next]) && Actor.findChar(next) == null) {
				cell = next;
				Dungeon.observe();
			}
		}
		
		if (cell != getPos()){
			final int tgt = cell;
			final Char ch = this;
			fx(cell, new Callback() {
				@Override
				public void call() {
					WandOfBlink.appear(ch, tgt);
				}
			});
		}
	}

	@Override
	public int defenseProc(Char enemy, int damage) {

		if (hp() > 2 * ht() / 3 && hp() - damage / 2 < 2 * ht() / 3) {
			blink(enemy.getPos());
			return damage / 2;
		}

		if (hp() > ht() / 3 && hp() - damage / 2 < ht() / 3) {
			blink(enemy.getPos());
			return damage / 2;
		}

		return damage;
	}

	@Override
	protected boolean canAttack(Char enemy) {
		return Ballistica.cast(getPos(), enemy.getPos(), false, true) == enemy.getPos();
	}

	protected boolean doAttack(Char enemy) {

		if (Dungeon.level.adjacent(getPos(), enemy.getPos())) {
			return super.doAttack(enemy);

		} else {
			boolean visible = Dungeon.level.fieldOfView[getPos()]
					|| Dungeon.level.fieldOfView[enemy.getPos()];
			if (visible) {
				getSprite().zap(enemy.getPos());
			}
			zap();

			return !visible;
		}
	}

	private void zap() {
		spend(TIME_TO_ZAP);

		if (hit(this, getEnemy(), true)) {
			if (getEnemy() == Dungeon.hero && Random.Int(2) == 0) {
				Buff.prolong(getEnemy(), Weakness.class, Weakness.duration(getEnemy()));
			}

			int dmg = Random.Int(12, 18);
			getEnemy().damage(dmg, this);

			if (!getEnemy().isAlive() && getEnemy() == Dungeon.hero) {
				Dungeon.fail(Utils.format(ResultDescriptions.MOB,
						Utils.indefinite(getName()), Dungeon.depth));
				GLog.n(TXT_SHADOWBOLT_KILLED, getName());
			}
		} else {
			getEnemy().getSprite().showStatus(CharSprite.NEUTRAL,
					getEnemy().defenseVerb());
		}
	}

	public void onZapComplete() {
		next();
	}

	@Override
	public void call() {
		next();
	}
}
