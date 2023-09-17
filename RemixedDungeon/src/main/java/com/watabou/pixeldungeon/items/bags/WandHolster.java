
package com.watabou.pixeldungeon.items.bags;

import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

public class WandHolster extends Bag {

	{
		image = ItemSpriteSheet.HOLSTER;
	}

	@Override
	public int price() {
		return 50;
	}
}
