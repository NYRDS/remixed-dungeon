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

import android.support.annotation.NonNull;

import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Amok;
import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.npcs.Imp;
import com.watabou.pixeldungeon.items.KindOfWeapon;
import com.watabou.pixeldungeon.items.food.Ration;
import com.watabou.pixeldungeon.items.weapon.melee.Knuckles;
import com.watabou.pixeldungeon.sprites.MonkSprite;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Random;

public class Monk extends Mob {

	public static final String TXT_DISARM = Game.getVar(R.string.Monk_Disarm);
	
	public Monk() {
		spriteClass = MonkSprite.class;
		
		hp(ht(70));
		defenseSkill = 30;
		
		exp = 11;
		maxLvl = 21;
		
		loot = new Ration();
		lootChance = 0.153f;
		
		IMMUNITIES.add( Amok.class );
		IMMUNITIES.add( Terror.class );
	}
	
	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 12, 16 );
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 30;
	}
	
	@Override
	protected float attackDelay() {
		return 0.5f;
	}
	
	@Override
	public int dr() {
		return 2;
	}
	
	@Override
	public void die( Object cause ) {
		Imp.Quest.process( this );
		
		super.die( cause );
	}
	
	@Override
	public int attackProc(@NonNull Char enemy, int damage ) {
		
		if (Random.Int( 6 ) == 0 && enemy == Dungeon.hero) {
			
			Hero hero = Dungeon.hero;
			KindOfWeapon weapon = hero.belongings.weapon;
			
			if (weapon != null && !(weapon instanceof Knuckles) && !weapon.cursed) {
				hero.belongings.weapon = null;
				Dungeon.level.drop( weapon, hero.getPos() ).sprite.drop();
				GLog.w( TXT_DISARM, getName(), weapon.name() );
			}
		}
		
		return damage;
	}
}
