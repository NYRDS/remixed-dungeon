package com.nyrds.retrodungeon.items.common.debug;

import com.nyrds.retrodungeon.ml.R;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.food.Food;

public class CandyOfDeath extends Food {

	// DIE, DIE, DIE, DIE

	public CandyOfDeath() {
		imageFile = "items/artifacts.png";
		image = 21;
	}

	@Override
	public void execute(Hero hero, String action ) {
		super.execute( hero, action );
		hero.damage(hero.ht(), this);
	}

	@Override
	public int price() {
		return 20 * quantity();
	}

}
