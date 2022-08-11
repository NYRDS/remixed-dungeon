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

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.weapon.Weapon;
import com.watabou.pixeldungeon.items.weapon.enchantments.Piercing;
import com.watabou.pixeldungeon.items.weapon.enchantments.Swing;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.sprites.MissileSprite;
import com.watabou.pixeldungeon.ui.QuickSlot;

import org.jetbrains.annotations.NotNull;

public class Boomerang extends MissileWeapon {

	{
		image = ItemSpriteSheet.BOOMERANG;
		
		setSTR(10);
		
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
	public void attackProc(Char attacker, Char defender, int damage ) {
		super.attackProc( attacker, defender, damage );
		if (attacker.rangedWeapon == this) {
			circleBack( defender.getPos(), attacker);
		}
	}

	@Override
	protected void miss(int cell, Char thrower) {
		circleBack( cell, thrower);
	}
	
	private void circleBack(int from, @NotNull final Char owner) {

		((MissileSprite) owner.getSprite().getParent()
				.recycle(MissileSprite.class)).reset(from, owner.getPos(),
				this, ()-> {
					if (throwSlot != Belongings.Slot.NONE) {
						owner.getBelongings().setItemForSlot(this, throwSlot);
					} else {
						owner.collect(this);
					}
					QuickSlot.refresh(owner);
				});
	}
	
	private Belongings.Slot throwSlot;
	
	@Override
	public void cast(@NotNull Char user, int dst ) {
		throwSlot = getOwner().getBelongings().itemSlot(this);
		super.cast( user, dst );
	}

	@Override
	public boolean isFliesStraight() {
		return false;
	}

	@Override
	public boolean isFliesFastRotating() {
		return true;
	}

	@Override
	public int price() {
		return 100;
	}

	@Override
	public float time2equipBase() {
		return 0;
	}
}
