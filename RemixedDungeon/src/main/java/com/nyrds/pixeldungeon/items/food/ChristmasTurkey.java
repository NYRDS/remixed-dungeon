package com.nyrds.pixeldungeon.items.food;

import com.watabou.pixeldungeon.actors.buffs.Hunger;
import com.watabou.pixeldungeon.items.food.Food;

public class ChristmasTurkey extends Food {

	public ChristmasTurkey() {
		imageFile = "items/food.png";
		image = 11;
		energy = Hunger.STARVING;
	}
	
	@Override
	public int price() {
		return 30 * quantity();
	}

}
