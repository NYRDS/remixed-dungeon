package com.nyrds.pixeldungeon.items.food;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;

abstract public class Fairycap extends Mushroom {
	{
		image = 1;
        message = StringsManager.getVar(R.string.Mushroom_Eat_Message);
	}

	@Override
	protected void applyEffect(Char hero){
		//TODO: + 1 max sp. А нызывается он Ведьмин Зонтик
	}
}
