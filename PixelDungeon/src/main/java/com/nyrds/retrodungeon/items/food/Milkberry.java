package com.nyrds.retrodungeon.items.food;

import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.hero.Hero;

abstract public class Milkberry extends Mushroom {
	{
		image = 4;
		message = Game.getVar(R.string.Mushroom_Eat_Message);
	}

	@Override
	protected void applyEffect(Hero hero){
		//TODO: Я его добавил исключительно ради названия и концепции. На грибе есть нарост, собрающий влагу, которая постепенно первращается в жидкость белого цвета со сладковатым, белковым вкусом.  А называется он Млековика
	}
}
