package com.watabou.pixeldungeon.items.food;

import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

public class RottenPasty extends RottenFood {
	public RottenPasty() {
		image   = ItemSpriteSheet.ROTTEN_PASTY;
	}
	
	@Override
	public Food purify() {
		return new Pasty();
	}
}
