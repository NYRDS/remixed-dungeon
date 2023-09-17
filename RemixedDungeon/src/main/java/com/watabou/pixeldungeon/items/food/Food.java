
package com.watabou.pixeldungeon.items.food;

import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.Item;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

abstract public class Food extends Item {

	public static final float TIME_TO_EAT	= 3f;

	public float energy   = 0;
	public String message = StringsManager.getVar(R.string.Food_Message);

    {
		stackable = true;
		setDefaultAction(CommonActions.AC_EAT);
	}
	
	@Override
	public ArrayList<String> actions(Char hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( CommonActions.AC_EAT );
		return actions;
	}


	@Override
	public void _execute(@NotNull Char chr, @NotNull String action ) {
		if (action.equals( CommonActions.AC_EAT )) {
			chr.eat(this, energy, message);
		} else {
			super._execute(chr, action );
		}
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	@Override
	public boolean isIdentified() {
		return true;
	}
}
