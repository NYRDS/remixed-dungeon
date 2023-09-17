
package com.watabou.pixeldungeon.items.weapon.melee;

import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

public class Mace extends MeleeWeapon {
	{
		image = ItemSpriteSheet.MACE;
		animation_class = SWORD_ATTACK;
	}
	
	public Mace() {
		super( 3, 1f, 0.8f );
	}

}
