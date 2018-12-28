package com.nyrds.pixeldungeon.items.food;

import com.watabou.pixeldungeon.items.food.Food;
import com.watabou.pixeldungeon.items.food.RottenFood;

public class RottenPumpkinPie extends RottenFood {
	public RottenPumpkinPie() {
		imageFile = "items/food.png";
		image = 10;
	}
	
	@Override
	public Food purify() {
		return new PumpkinPie();
	}
}
