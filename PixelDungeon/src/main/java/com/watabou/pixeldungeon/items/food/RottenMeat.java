package com.watabou.pixeldungeon.items.food;

import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

public class RottenMeat extends RottenFood {
	public RottenMeat() {
		image   = ItemSpriteSheet.ROTTEN_MEAT;
	}
	
	@Override
	public Food purify() {
		return new MysteryMeat();
	}
}
