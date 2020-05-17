package com.nyrds.pixeldungeon.items.common.debug;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.food.Food;

public class CandyOfDeath extends Food {

	// DIE, DIE, DIE, DIE

	public CandyOfDeath() {
		imageFile = "items/artifacts.png";
		image = 21;
	}

	@Override
	public void execute(Char chr, String action ) {
		super.execute(chr, action );
		chr.damage(chr.ht(), this);
	}

	@Override
	public int price() {
		return 20 * quantity();
	}

}
