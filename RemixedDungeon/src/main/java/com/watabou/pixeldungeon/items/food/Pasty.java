
package com.watabou.pixeldungeon.items.food;

import com.watabou.pixeldungeon.actors.buffs.Hunger;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

public class Pasty extends Food {

	public Pasty() {
		image  = ItemSpriteSheet.PASTY;
		energy = Hunger.STARVING;
	}
	
	@Override
	public int price() {
		return 25 * quantity();
	}
	
	@Override
	public Item poison(int cell){
		return morphTo(RottenPasty.class);
	}
}
