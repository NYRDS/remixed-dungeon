package com.nyrds.retrodungeon.items.food;

import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.food.Food;
import com.watabou.pixeldungeon.utils.GLog;

abstract public class Mushroom extends Food {
	{
		imageFile = "items/shrooms";
		stackable = true;
		message = Game.getVar(R.string.Mushroom_Eat_Message);
	}

	@Override
	public void execute( Hero hero, String action ) {
		if (action.equals( AC_EAT )) {
			applyEffect(hero);
			GLog.i( message );
		} else {
			super.execute( hero, action );
		}
	}

	protected void applyEffect(Hero hero){}
}
