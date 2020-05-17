package com.nyrds.pixeldungeon.items.food;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.CommonActions;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.food.Food;
import com.watabou.pixeldungeon.utils.GLog;

abstract public class Mushroom extends Food {
	{
		imageFile = "items/shrooms";
		stackable = true;
		message = Game.getVar(R.string.Mushroom_Eat_Message);
	}

	@Override
	public void execute(Char chr, String action ) {
		if (action.equals( CommonActions.AC_EAT )) {
			applyEffect(chr);
			GLog.i( message );
		} else {
			super.execute(chr, action );
		}
	}

	protected void applyEffect(Char hero){}
}
