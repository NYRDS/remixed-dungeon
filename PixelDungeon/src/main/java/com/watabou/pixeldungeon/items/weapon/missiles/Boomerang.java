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
package com.watabou.pixeldungeon.items.weapon.missiles;
import com.watabou.noosa.Game;
import com.nyrds.retrodungeon.ml.R;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.weapon.Weapon;
import com.watabou.pixeldungeon.items.weapon.enchantments.Piercing;
import com.watabou.pixeldungeon.items.weapon.enchantments.Swing;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.sprites.MissileSprite;
import com.watabou.utils.Callback;

public class Boomerang extends MissileWeapon {

	{
		name = Game.getVar(R.string.Boomerang_Name);
		image = ItemSpriteSheet.BOOMERANG;
		
		STR = 10;
		
		MIN = 1;
		MAX = 4;
		
		stackable = false;
	}
	
	@Override
	public boolean isUpgradable() {
		return true;
	}
	
	@Override
	public Item upgrade() {
		return upgrade( false );
	}
	
	@Override
	public Item upgrade( boolean enchant ) {
		MIN += 1;
		MAX += 2;
		super.upgrade( enchant );
		
		updateQuickslot();
		
		return this;
	}
	
	@Override
	public Item degrade() {
		MIN -= 1;
		MAX -= 2;
		return super.degrade();
	}
	
	@Override
	public Weapon enchant( Enchantment ench ) {
		while (ench instanceof Piercing || ench instanceof Swing) {
			ench = Enchantment.random();
		}
		
		return super.enchant( ench );
	}
	
	@Override
	public void proc( Char attacker, Char defender, int damage ) {
		super.proc( attacker, defender, damage );
		if (attacker instanceof Hero && ((Hero)attacker).rangedWeapon == this) {
			circleBack( defender.getPos(), (Hero)attacker );
		}
	}
	
	@Override
	protected void miss( int cell ) {
		circleBack( cell, getCurUser() );
	}
	
	private void circleBack(int from, final Hero owner) {

		((MissileSprite) getCurUser().getSprite().getParent()
				.recycle(MissileSprite.class)).reset(from, getCurUser().getPos(),
				curItem, new Callback() {
					@Override
					public void call() {
						if (throwEquiped) {
							owner.belongings.weapon = Boomerang.this;
						} else {
							getCurUser().collect(Boomerang.this);
						}
						updateQuickslot();
					}
				});

		if (throwEquiped) {
			owner.spend(-TIME_TO_EQUIP);
		}
	}
	
	private boolean throwEquiped;
	
	@Override
	public void cast( Hero user, int dst ) {
		throwEquiped = isEquipped( user );
		super.cast( user, dst );
	}
	
	@Override
	public String desc() {
		return Game.getVar(R.string.Boomerang_Info);
	}
	
	@Override
	public boolean isFliesStraight() {
		return false;
	}

	@Override
	public boolean isFliesFastRotating() {
		return true;
	}
}
