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

import com.nyrds.pixeldungeon.ai.Hunting;
import com.nyrds.pixeldungeon.mobs.common.IZapper;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Cripple;
import com.watabou.pixeldungeon.actors.buffs.Poison;
import com.watabou.pixeldungeon.items.food.MysteryMeat;
import com.watabou.pixeldungeon.items.potions.PotionOfHealing;
import com.watabou.pixeldungeon.items.weapon.enchantments.Leech;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class Scorpio extends Mob implements IZapper {
	
	public Scorpio() {

		hp(ht(95));
		baseDefenseSkill = 24;
		baseAttackSkill  = 36;
		
		exp = 14;
		maxLvl = 25;


		if (Random.Int( 8 ) == 0) {
			collect(new PotionOfHealing());
		} else if (Random.Int( 6 ) == 0) {
			collect(new MysteryMeat());
		}

		addResistance( Leech.class );
		addResistance( Poison.class );
	}

	@Override
	public void onSpawn(Level level) {
		super.onSpawn(level);
		setViewDistance(level.getViewDistance() + 1);
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 20, 32 );
	}

	@Override
	public int dr() {
		return 16;
	}
	
	@Override
    public boolean canAttack(@NotNull Char enemy) {
		return CharUtils.canDoOnlyRangedAttack(this, enemy);
	}

	@Override
	protected int zapProc(@NotNull Char enemy, int damage) {
		if (Random.Int( 2 ) == 0) {
			Buff.prolong( enemy, Cripple.class, Cripple.DURATION );
		}
		return damage;
	}

	@Override
	public boolean getCloser(int target) {
		if (getState() instanceof Hunting) {
			return enemySeen && getFurther( target );
		} else {
			return super.getCloser( target );
		}
	}
}
