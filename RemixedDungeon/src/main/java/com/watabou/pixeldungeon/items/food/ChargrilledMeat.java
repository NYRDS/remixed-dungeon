
package com.watabou.pixeldungeon.items.food;

import com.watabou.pixeldungeon.actors.buffs.Hunger;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

public class ChargrilledMeat extends Food {

	{
		image = ItemSpriteSheet.STEAK;
		energy = Hunger.STARVING - Hunger.HUNGRY;
	}
	
	@Override
	public int price() {
		return 7 * quantity();
	}
	
	@Override
	public Item poison(int cell){
		return morphTo(RottenMeat.class);
	}
}
