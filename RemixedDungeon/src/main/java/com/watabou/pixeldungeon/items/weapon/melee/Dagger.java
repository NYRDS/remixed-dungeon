
package com.watabou.pixeldungeon.items.weapon.melee;

import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

public class Dagger extends MeleeWeapon {
	{
		image = ItemSpriteSheet.DAGGER;
		animation_class = SWORD_ATTACK;
	}
	
	public Dagger() {
		super( 1, 1.2f, 1f );
	}
}
