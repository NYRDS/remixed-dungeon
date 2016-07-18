package com.nyrds.pixeldungeon.items.necropolis;

import com.watabou.pixeldungeon.items.weapon.melee.SpecialWeapon;
import com.watabou.pixeldungeon.sprites.ItemSprite.Glowing;

//This weapon supposed to cast projectiles towards target enemy, so that they would hit first thing in they're path
public class BladeOfSouls extends SpecialWeapon {
	{
		imageFile = "items/swords.png";
		image = 7;
		enchatable = false;
	}

	public BladeOfSouls() {
		super( 3, 1f, 1f );
	}
	
	@Override
	public Glowing glowing() {
		float period = 1;
		return new Glowing(0xaaaaff, period);
	}
}
