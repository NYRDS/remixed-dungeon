package com.nyrds.retrodungeon.items.food;

import com.watabou.pixeldungeon.actors.buffs.Hunger;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.food.Food;

public class PumpkinPie extends Food {

	public PumpkinPie() {
		imageFile = "items/food.png";
		image = 9;
		energy = Hunger.STARVING;
	}
	
	@Override
	public int price() {
		return 20 * quantity();
	}
	
	@Override
	public Item poison(int cell){
		return morphTo(RottenPumpkinPie.class);
	}
}
