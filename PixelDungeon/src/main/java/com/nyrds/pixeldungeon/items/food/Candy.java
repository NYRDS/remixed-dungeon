package com.nyrds.pixeldungeon.items.food;

import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.food.Food;

public class Candy extends Food {

	public Candy() {
		imageFile = "items/artifacts.png";
		image = 21;
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
