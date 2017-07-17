package com.nyrds.pixeldungeon.items.common.debug;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.effects.SpellSprite;
import com.watabou.pixeldungeon.items.food.Food;
import com.watabou.pixeldungeon.utils.GLog;

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
