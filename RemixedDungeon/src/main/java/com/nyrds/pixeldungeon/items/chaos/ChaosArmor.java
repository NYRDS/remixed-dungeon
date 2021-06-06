package com.nyrds.pixeldungeon.items.chaos;

import com.nyrds.Packable;
import com.watabou.pixeldungeon.items.armor.Armor;
import com.watabou.utils.Bundle;


public class ChaosArmor extends Armor {

	@Packable
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
		return (int) (5 * Math.pow(level(), 1.5));
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
				inscribe(null);
				charge = chargeForLevel();
				selectImage();
			}
		}
	}

	@Override
	public void ownerDoesDamage(int damage) {

		if(isCursed()) {
			return;
		}

		if(damage > 0) {
			charge++;
			if(charge > chargeForLevel()) {
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
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);

		selectImage();
	}
}
