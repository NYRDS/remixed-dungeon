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

import com.nyrds.pixeldungeon.items.Treasury;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.common.BlinkAwayFromChar;
import com.nyrds.pixeldungeon.mobs.common.IZapper;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Weakness;
import com.watabou.pixeldungeon.effects.MagicMissile;
import com.watabou.pixeldungeon.items.weapon.enchantments.Death;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class Warlock extends Mob implements IZapper {

	public Warlock() {
		hp(ht(70));
		baseDefenseSkill = 18;
		baseAttackSkill  = 25;

		exp = 11;
		maxLvl = 21;

		loot(Treasury.Category.POTION, 0.83f);

		addResistance(Death.class);
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange(12, 20);
	}

	@Override
	public int dr() {
		return 8;
	}

	protected void fx( int cell, Callback callback ) {
		if(getSprite().getParent()==null) {
			EventCollector.logException("null parent");
			return;
		}
		MagicMissile.whiteLight( getSprite().getParent(), getPos(), cell, callback );
		Sample.INSTANCE.play( Assets.SND_ZAP );
		getSprite().setVisible(false);
	}

	@Override
	public int defenseProc(Char enemy, int damage) {

		if (hp() > 2 * ht() / 3 && hp() - damage / 2 < 2 * ht() / 3) {
			CharUtils.blinkAway(this,
					new BlinkAwayFromChar(enemy,2));
			return damage / 2;
		}

		if (hp() > ht() / 3 && hp() - damage / 2 < ht() / 3) {
			CharUtils.blinkAway(this, new BlinkAwayFromChar(enemy,3));
			return damage / 2;
		}

		return damage;
	}

	@Override
    public boolean canAttack(@NotNull Char enemy) {
		return Ballistica.cast(getPos(), enemy.getPos(), false, true) == enemy.getPos();
	}

	@Override
	public boolean zap(@NotNull Char enemy) {
		if (super.zap(enemy)) {
			if (getEnemy() == Dungeon.hero && Random.Int(2) == 0) {
				Buff.prolong(getEnemy(), Weakness.class, Weakness.duration(getEnemy()));
			}

			CharUtils.checkDeathReport(this, enemy, Game.getVar(R.string.Warlock_Killed));

			return true;
		}
		return false;
	}

}
