
package com.watabou.pixeldungeon.items.bags;

import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

public class ScrollHolder extends Bag {

	{
		image = ItemSpriteSheet.HOLDER;
	}

	@Override
	public int price() {
		return 50;
	}
}
