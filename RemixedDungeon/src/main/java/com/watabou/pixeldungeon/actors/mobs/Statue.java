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
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Journal;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Bleeding;
import com.watabou.pixeldungeon.actors.buffs.Poison;
import com.watabou.pixeldungeon.items.Generator;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfPsionicBlast;
import com.watabou.pixeldungeon.items.weapon.Weapon;
import com.watabou.pixeldungeon.items.weapon.Weapon.Enchantment;
import com.watabou.pixeldungeon.items.weapon.enchantments.Death;
import com.watabou.pixeldungeon.items.weapon.enchantments.Leech;
import com.watabou.pixeldungeon.items.weapon.melee.Dagger;
import com.watabou.pixeldungeon.items.weapon.melee.MeleeWeapon;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.sprites.HeroSpriteDef;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import androidx.annotation.NonNull;

public class Statue extends Mob {
	
	protected Weapon weapon;
	
	public Statue() {
		exp = 0;
		setState(PASSIVE);
		
		do {
			weapon = (Weapon)Generator.random( Generator.Category.WEAPON );
		} while (!(weapon instanceof MeleeWeapon) || weapon.level() < 0);
		
		weapon.identify();
		weapon.enchant( Enchantment.random() );
		
		hp(ht(15 + Dungeon.depth * 5));
		defenseSkill = 4 + Dungeon.depth;

		IMMUNITIES.add( ToxicGas.class );
		IMMUNITIES.add( Poison.class );
		RESISTANCES.add( Death.class );
		RESISTANCES.add( ScrollOfPsionicBlast.class );
		IMMUNITIES.add( Leech.class );
		IMMUNITIES.add(Bleeding.class);
	}
	
	private static final String WEAPON	= "weapon";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( WEAPON, weapon );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		weapon = (Weapon)bundle.get( WEAPON );
	}
	
	@Override
	protected boolean act() {
		if (!isPet() && Dungeon.visible[getPos()]) {
			Journal.add( Journal.Feature.STATUE.desc() );
		}
		return super.act();
	}
	
	@Override
	public int damageRoll() {
		return Random.NormalIntRange( weapon.MIN, weapon.MAX );
	}
	
	@Override
	public int attackSkill( Char target ) {
		return (int)((9 + Dungeon.depth) * weapon.ACU);
	}
	
	@Override
	protected float attackDelay() {
		return weapon.DLY;
	}
	
	@Override
	public int dr() {
		return Dungeon.depth;
	}

	@Override
	public int attackProc(@NonNull Char enemy, int damage ) {
		weapon.proc( this, enemy, damage );
		return damage;
	}
	
	@Override
	public void beckon( int cell ) {
	}
	
	@Override
	public void die( Object cause ) {
		Dungeon.level.drop( weapon, getPos() ).sprite.drop();
		super.die( cause );
	}
	
	@Override
	public void destroy() {
		Journal.remove( Journal.Feature.STATUE.desc() );
		super.destroy();
	}
	
	@Override
	public boolean reset() {
		setState(PASSIVE);
		return true;
	}

	@Override
	public String description() {
		return Utils.format(Game.getVar(R.string.Statue_Desc), weapon.name());
	}

	@Override
	public CharSprite sprite() {
		if(weapon==null) {
			weapon = new Dagger();
			EventCollector.logException("no weapon");
		}

		return HeroSpriteDef.createHeroSpriteDef(new Dagger());
	}
}
