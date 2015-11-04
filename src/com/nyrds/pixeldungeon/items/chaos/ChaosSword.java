package com.nyrds.pixeldungeon.items.chaos;

import com.watabou.pixeldungeon.items.weapon.melee.SpecialWeapon;
import com.watabou.utils.Bundle;

public class ChaosSword extends SpecialWeapon implements IChaosItem {

	private int charge = 0;
	
	public ChaosSword() {
		super(3, 1, 1);
		
		imageFile = "items/chaosSword.png";
		image = 0;
		upgrade(true);
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	@Override
	public void ownerTakesDamage(int damage) {
		
	}

	@Override
	public void ownerDoesDamage(int damage) {
		
		if(cursed) {
			return;
		}
		
		if(damage > 0) {
			charge+=10;
			if(charge>100) {
				upgrade();
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
}
