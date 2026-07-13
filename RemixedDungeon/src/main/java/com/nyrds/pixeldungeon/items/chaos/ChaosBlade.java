package com.nyrds.pixeldungeon.items.chaos;

import com.nyrds.Packable;
import com.watabou.pixeldungeon.items.weapon.melee.MeleeWeapon;
import com.watabou.utils.Bundle;

// One-handed variant of ChaosSword: lower tier, slower progression,
// earlier degradation — but leaves LEFT_HAND free for shields/dual-wield.
public class ChaosBlade extends MeleeWeapon {

	@Packable
	public int charge = 0;

	public ChaosBlade() {
		super(2, 1, 1);

		imageFile = "items/chaosSword.png";
		image = 0;
		animation_class = SWORD_ATTACK;
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	private int chargeForLevel() {
		return (int) (7 * Math.pow(level(), 1.5));
	}

	@Override
	public void ownerTakesDamage(int damage) {
		charge--;
		if (charge < 0) {
			charge = 0;
		}

		if (level() > 2) {
			if (charge == 0) {
				degrade();
				enchant(null);
				charge = chargeForLevel();
				selectImage();
			}
		}
	}

	@Override
	public void ownerDoesDamage(int damage) {

		if (isCursed()) {
			return;
		}

		if (damage > 0) {
			charge++;
			if (charge > chargeForLevel()) {
				upgrade(true);
				selectImage();
				charge = 0;
			}
		}
	}

	private void selectImage() {
		image = Math.max(0, Math.min(level() / 3, 4));
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);

		selectImage();
	}
}
