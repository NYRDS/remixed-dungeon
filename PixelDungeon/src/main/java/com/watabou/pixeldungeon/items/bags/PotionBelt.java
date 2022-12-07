package com.watabou.pixeldungeon.items.bags;

import com.nyrds.retrodungeon.items.drinks.ManaPotion;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.potions.Potion;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

public class PotionBelt extends Bag {

	{
		image = ItemSpriteSheet.BELT;
	}
	
	@Override
	public boolean grab( Item item ) {
		return item instanceof Potion || item instanceof ManaPotion;
	}
	
	@Override
	public int price() {
		return 50;
	}
	
}
