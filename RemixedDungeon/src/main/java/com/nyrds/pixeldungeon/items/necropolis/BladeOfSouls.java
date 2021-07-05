package com.nyrds.pixeldungeon.items.necropolis;

import com.watabou.pixeldungeon.items.weapon.melee.MeleeWeapon;
import com.watabou.pixeldungeon.sprites.Glowing;

//This weapon supposed to cast projectiles towards target enemy, so that they would hit first thing in they're path
public class BladeOfSouls extends MeleeWeapon {
	{
		imageFile = "items/swords.png";
		image = 7;
		enchatable = false;
		MIN = (int)(max() * 1.2);
		MAX = (int)(max() * 1.4);
	}

	public BladeOfSouls() {
		super( 3, 1.3f, 0.8f );
	}
	
	@Override
	public Glowing glowing() {
		float period = 1;
		return new Glowing(0xaaaaff, period);
	}
}
