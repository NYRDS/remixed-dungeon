package com.watabou.pixeldungeon.items.food;

import com.watabou.pixeldungeon.actors.buffs.Hunger;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

public class Ration extends Food {
	{
		image = ItemSpriteSheet.RATION;
		energy = Hunger.HUNGRY;
	}
	
	@Override
	public int price() {
		return 10 * quantity();
	}
	
	@Override
	public Item poison(int cell){
		return morphTo(RottenRation.class);
	}
}
