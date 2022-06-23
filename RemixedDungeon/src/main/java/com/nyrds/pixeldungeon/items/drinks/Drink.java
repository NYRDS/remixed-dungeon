package com.nyrds.pixeldungeon.items.drinks;

import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.bags.PotionBelt;

import java.util.ArrayList;

abstract public class Drink extends Item {

	public static final float TIME_TO_DRINK	= 1f;

	{
		stackable = true;
		setDefaultAction(CommonActions.AC_DRINK);
	}
	
	@Override
	public ArrayList<String> actions(Char hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add(CommonActions.AC_DRINK);
		return actions;
	}

	
	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public String bag() {
		return PotionBelt.class.getSimpleName();
	}
}
