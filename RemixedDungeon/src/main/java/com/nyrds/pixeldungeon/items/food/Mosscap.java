package com.nyrds.pixeldungeon.items.food;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;

abstract public class Mosscap extends Mushroom {
	{
		image = 3;
        message = StringsManager.getVar(R.string.Mushroom_Eat_Message);
	}

	@Override
	protected void applyEffect(Char hero){
		//TODO: + 1 str.  А называется он Жизневик Кудрявый
	}
}
