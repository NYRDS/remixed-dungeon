package com.watabou.pixeldungeon.items.bags;

import com.nyrds.pixeldungeon.items.drinks.ManaPotion;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.potions.Potion;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

import org.jetbrains.annotations.NotNull;

public class PotionBelt extends Bag {

	{
		image = ItemSpriteSheet.BELT;
	}
	
	@Override
	public boolean grab(@NotNull Item item ) {
		return super.grab(item) || item instanceof Potion || item instanceof ManaPotion;
	}
	
	@Override
	public int price() {
		return 50;
	}
	
}
