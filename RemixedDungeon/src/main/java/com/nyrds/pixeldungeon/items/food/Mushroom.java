package com.nyrds.pixeldungeon.items.food;

import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.food.Food;
import com.watabou.pixeldungeon.utils.GLog;

import org.jetbrains.annotations.NotNull;

abstract public class Mushroom extends Food {
	{
		imageFile = "items/shrooms";
		stackable = true;
        message = StringsManager.getVar(R.string.Mushroom_Eat_Message);
	}

	@Override
	public void _execute(@NotNull Char chr, @NotNull String action ) {
		if (action.equals( CommonActions.AC_EAT )) {
			applyEffect(chr);
			GLog.i( message );
		} else {
			super._execute(chr, action );
		}
	}

	protected void applyEffect(Char hero){}
}
