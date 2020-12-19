package com.nyrds.pixeldungeon.items.common.debug;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.food.Food;

import org.jetbrains.annotations.NotNull;

public class CandyOfDeath extends Food {

	// DIE, DIE, DIE, DIE

	public CandyOfDeath() {
		imageFile = "items/artifacts.png";
		image = 21;
	}

	@Override
	public void _execute(@NotNull Char chr, @NotNull String action ) {
		super._execute(chr, action );
		chr.damage(chr.ht(), this);
	}

	@Override
	public int price() {
		return 20 * quantity();
	}

}
