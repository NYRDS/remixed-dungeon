package com.nyrds.pixeldungeon.items.food;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;

abstract public class Milkberry extends Mushroom {
	{
		image = 4;
        message = StringsManager.getVar(R.string.Mushroom_Eat_Message);
	}

	@Override
	protected void applyEffect(Char hero){
		//TODO: Я его добавил исключительно ради названия и концепции. На грибе есть нарост, собрающий влагу, которая постепенно первращается в жидкость белого цвета со сладковатым, белковым вкусом.  А называется он Млековика
	}
}
