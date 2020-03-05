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
package com.watabou.pixeldungeon.items;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.utils.GLog;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public abstract class EquipableItem extends Item {

	public static final String NO_ANIMATION = "none";
	protected static final String AC_EQUIP   = "EquipableItem_ACEquip";
	protected static final String AC_UNEQUIP = "EquipableItem_ACUnequip";

	@Override
	public void execute( Hero hero, String action ) {
		switch (action) {
			case AC_EQUIP:
				doEquip(hero);
				break;
			case AC_UNEQUIP:
				doUnequip(hero, true);
				break;
			default:
				super.execute(hero, action);
				break;
		}
	}
	
	@Override
	public void doDrop( Char hero ) {
		if (!isEquipped( hero ) || doUnequip( hero, false, false )) {
			super.doDrop( hero );
		}
	}
	
	@Override
	public void cast(final Char user, int dst ) {

		if (isEquipped( user )) {
			if (quantity() == 1 && !this.doUnequip( user, false, false )) {
				return;
			}
		}
		
		super.cast( user, dst );
	}

	public float time2equip(@NotNull Char hero ) {
		return 1f/(hero.speed()+0.01f);
	}

	@Override
	public ArrayList<String> actions(Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( isEquipped( hero ) ? AC_UNEQUIP : AC_EQUIP );
		return actions;
	}

	public boolean doEquip(Hero hero ) {
		setUser(hero);
		Belongings belongings = hero.getBelongings();
		return belongings.equip(this, slot(belongings));
	}

	public void activate(Char ch) {}
	public void deactivate(Char ch) {}

	protected boolean doUnequip(Char hero, boolean collect, boolean single) {
		
		if (cursed) {
			GLog.w( Game.getVar(R.string.EquipableItem_Unequip), name() );
			return false;
		}

		Belongings belongings = hero.getBelongings();

		if(!belongings.unequip(this)) {
			return false;
		}

		if (single) {
			hero.spendAndNext( time2equip( hero ) );
		} else {
			hero.spend( time2equip( hero ) );
		}
		
		if (collect && !collect( hero.getBelongings().backpack )) {
			hero.level().drop( this, hero.getPos() );
		}
				
		return true;
	}

	public boolean doUnequip( Char hero, boolean collect ) {
		return doUnequip( hero, collect, true );
	}

	public String getVisualName() {
		return getClassName();
	}

	public abstract Belongings.Slot slot(Belongings belongings);

	public int typicalSTR() {
		return 0;
	}

	public Belongings.Slot blockSlot() {
		return Belongings.Slot.NONE;
	}


	public float accuracyFactor(Char user) {
		return 1f;
	}

	public float impactDelayFactor(Char user, float delayFactor) {
		float ownFactor = attackDelayFactor(user);
		return Math.max(ownFactor * 1.5f, ownFactor * 0.75f + delayFactor * 0.75f);
	}

	public float impactAccuracyFactor(Char user, float accuracyFactor) {
		return Math.min(accuracyFactor(user), accuracyFactor);
	}

	//dual
	public float attackDelayFactor(Char user) {
		return 1f;
	}

	//dual
	public int damageRoll(Char user) {
		return 0;
	}

	//dual
	public void attackProc(Char attacker, Char defender, int damage ) {
	}

	public String getAttackAnimationClass() {
		return NO_ANIMATION;
	}

	public boolean goodForMelee() {
		return true;
	}

	public void equippedCursed() {
		GLog.n(Game.getVar(R.string.KindOfWeapon_EquipCursed), name() );
	}

	public int requiredSTR() {
		return 0;
	}
}
