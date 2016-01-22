package com.watabou.pixeldungeon.items.food;

import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

public class RottenRation extends RottenFood {
	public RottenRation() {
		image   = ItemSpriteSheet.ROTTEN_RATION;
	}
	
	@Override
	public Food purify() {
		return new Ration();
	}
}
