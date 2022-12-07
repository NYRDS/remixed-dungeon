package com.nyrds.retrodungeon.items.chaos;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.weapon.melee.SpecialWeapon;
import com.watabou.utils.Bundle;

public class ChaosSword extends SpecialWeapon implements IChaosItem {

	private int charge = 0;
	
	public ChaosSword() {
		super(3, 1, 1);
		
		imageFile = "items/chaosSword.png";
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
				enchant(null);
				charge = chargeForLevel();
				selectImage();
			}
		}
	}
	
	@Override
	public void ownerDoesDamage(Char ch,int damage) {
		
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
