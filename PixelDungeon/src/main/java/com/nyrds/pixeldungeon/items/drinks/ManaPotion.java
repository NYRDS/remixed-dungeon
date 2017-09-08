package com.nyrds.pixeldungeon.items.drinks;

import com.watabou.pixeldungeon.actors.buffs.Hunger;
import com.watabou.pixeldungeon.items.food.Food;

public class ManaPotion extends Drink {

	public ManaPotion() {
		imageFile = "items/drinks.png";
		image = 0;
		mana = 10;
	}
	
	@Override
	public int price() {
		return 100 * quantity();
	}

}
