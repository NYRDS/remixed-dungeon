package com.nyrds.retrodungeon.items.food;

import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.hero.Hero;

abstract public class Fairycap extends Mushroom {
	{
		image = 1;
		message = Game.getVar(R.string.Mushroom_Eat_Message);
	}

	@Override
	protected void applyEffect(Hero hero){
		//TODO: + 1 max sp. А нызывается он Ведьмин Зонтик
	}
}
