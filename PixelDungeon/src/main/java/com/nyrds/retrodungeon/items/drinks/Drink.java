package com.nyrds.retrodungeon.items.drinks;

import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Item;

import java.util.ArrayList;

abstract public class Drink extends Item {

	public static final float TIME_TO_DRINK	= 1f;
	
	public static final String AC_DRINK = Game.getVar(R.string.Drink_ACDrink);

	public String message 	= Game.getVar(R.string.Drink_Message);
	
	{
		stackable = true;
		defaultAction = AC_DRINK;
	}
	
	@Override
	public ArrayList<String> actions( Hero hero ) {
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
}
