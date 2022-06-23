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

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.utils.GLog;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public abstract class EquipableItem extends Item {

	public static final String NO_ANIMATION = "none";

	protected int gender;

	@NotNull
	protected Belongings.Slot equipedTo = Belongings.Slot.NONE;

	@Override
	public void _execute(@NotNull Char chr, @NotNull String action ) {
		switch (action) {
			case CommonActions.AC_EQUIP:
				doEquip(chr);
				break;
			case CommonActions.AC_UNEQUIP:
				doUnequip(chr, true);
				break;
			default:
				super._execute(chr, action);
				break;
		}
	}
	
	@Override
	public void doDrop(@NotNull Char hero ) {
		if (!isEquipped( hero ) || doUnequip( hero, false, false )) {
			super.doDrop( hero );
		}
	}
	
	@Override
	public void cast(final @NotNull Char user, int dst ) {

		if (isEquipped( user )) {
			if (quantity() == 1 && !this.doUnequip( user, false, false )) {
				return;
			}
		}
		
		super.cast( user, dst );
	}

	public float time2equip(@NotNull Char hero ) {
		return time2equipBase()/(hero.speed()+0.01f);
	}


	public float time2equipBase() {
		return 1;
	}

	@Override
	public ArrayList<String> actions(Char hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add(isEquipped(hero) ? CommonActions.AC_UNEQUIP : CommonActions.AC_EQUIP);
		return actions;
	}

	public boolean doEquip(@NotNull Char hero ) {
		Belongings belongings = hero.getBelongings();
		return belongings.equip(this, slot(belongings));
	}

	public void activate(@NotNull Char ch) {
		equipedTo = ch.getBelongings().itemSlot(this);
	}

	public void deactivate(@NotNull Char ch) {
		equipedTo = Belongings.Slot.NONE;
		Buff.detachAllBySource(ch,this);
	}

	@LuaInterface
	public String slotName() {
		return equipedTo.name();
	}

	protected boolean doUnequip(Char hero, boolean collect, boolean single) {
		
		if (isCursed()) {
            GLog.w(StringsManager.getVar(R.string.EquipableItem_Unequip), name() );
			return false;
		}

		Belongings belongings = hero.getBelongings();

		if(!belongings.unequip(this)) {
			return false;
		}

		hero.spend( time2equip( hero ) );
		
		if (collect && !collect( hero.getBelongings().backpack )) {
			doDrop(hero);
		}
				
		return true;
	}

	public boolean doUnequip( Char hero, boolean collect ) {
		return doUnequip( hero, collect, true );
	}

	public String getVisualName() {
		return getEntityKind();
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
        GLog.n(StringsManager.getVar(R.string.KindOfWeapon_EquipCursed), name() );
	}

	public int requiredSTR() {
		return 0;
	}

	public int effectiveDr() {
		return 0;
	}

    public int defenceProc(Char attacker, Char defender, int damage) {
		return damage;
    }

    //Armor visuals
	public boolean hasHelmet(){
		return false;
	}
	public boolean hasCollar() {
		return false;
	}
	public boolean isCoveringHair() {
		return false;
	}
	public boolean isCoveringFacialHair() {return false;}

	//former SpecialWeapon
	public int range() {
		return 1;
	}

	public void preAttack(Char tgt ) {
	}

	public void postAttack(Char tgt ) {
	}
}
