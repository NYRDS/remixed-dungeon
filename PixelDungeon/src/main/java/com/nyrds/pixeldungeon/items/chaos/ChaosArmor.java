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
package com.nyrds.pixeldungeon.items.chaos;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.armor.Armor;
import com.watabou.utils.Bundle;


public class ChaosArmor extends Armor implements IChaosItem {

	private int charge = 0;

	public ChaosArmor() {
		super( 3 );
		imageFile = "items/chaosArmor.png";
		image = 0;
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	private int chargeForLevel() {
		return (int) (5 * Math.pow(level(), 1.4));
	}

	@Override
	public void ownerTakesDamage(int damage) {
		charge--;
		if(charge < 0) {
			charge = 0;
		}

		if(level() > 3) {
			if(charge == 0) {
				degrade();
				charge = chargeForLevel();
				selectImage();
			}
		}
	}

	@Override
	public void ownerDoesDamage(Char ch, int damage) {

		if(cursed) {
			return;
		}

		if(damage > 0) {
			charge++;
			if(charge > chargeForLevel()) {
				upgrade(true);
				upgrade(true);
				selectImage();
				charge = 0;
			}
		}
	}

	private void selectImage() {
		image = Math.max(0, Math.min(level()/3, 4));
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);

		bundle.put(ChaosCommon.CHARGE_KEY, charge);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);

		charge = bundle.getInt(ChaosCommon.CHARGE_KEY);
	}

	@Override
	public int getCharge() {
		return charge;
	}

}
