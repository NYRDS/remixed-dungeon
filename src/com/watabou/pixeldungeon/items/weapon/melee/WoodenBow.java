package com.watabou.pixeldungeon.items.weapon.melee;

import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

public class WoodenBow extends Bow {

	public WoodenBow() {
		super( 1, 0.8f, 1.5f );
		image = ItemSpriteSheet.BOW_WOODEN;
	}
	
}
