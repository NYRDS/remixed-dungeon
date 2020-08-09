package com.nyrds.pixeldungeon.items.drinks;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.bags.PotionBelt;

import java.util.ArrayList;

abstract public class Drink extends Item {

	public static final float TIME_TO_DRINK	= 1f;
	
	public static final String AC_DRINK = "Drink_ACDrink";

	{
		stackable = true;
		setDefaultAction(AC_DRINK);
	}
	
	@Override
	public ArrayList<String> actions(Char hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_DRINK );
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
