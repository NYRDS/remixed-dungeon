package com.nyrds.retrodungeon.items.food;

import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.hero.Hero;

abstract public class LifeBolete extends Mushroom {
	{
		image = 0;
		message = Game.getVar(R.string.Mushroom_Eat_Message);
	}

	@Override
	protected void applyEffect(Hero hero){
		//TODO: + 1 max hp.  А называется он Жизневик
	}
}
