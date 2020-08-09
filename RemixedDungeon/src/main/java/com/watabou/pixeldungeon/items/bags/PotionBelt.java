package com.watabou.pixeldungeon.items.bags;

import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

public class PotionBelt extends Bag {

	{
		image = ItemSpriteSheet.BELT;
	}

	@Override
	public int price() {
		return 50;
	}
}
