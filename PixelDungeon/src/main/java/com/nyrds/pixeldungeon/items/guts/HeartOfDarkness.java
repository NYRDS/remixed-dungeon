package com.nyrds.pixeldungeon.items.guts;

import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.MindVision;
import com.watabou.pixeldungeon.actors.buffs.Vertigo;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.food.Food;

import java.util.ArrayList;

public class HeartOfDarkness extends Item {

	public HeartOfDarkness() {
		imageFile = "items/artifacts.png";
		image = 18;
		identify();
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(Food.AC_EAT);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action ) {

		super.execute( hero, action );

		if (action.equals( Food.AC_EAT )) {
			Buff.affect(hero, Vertigo.class, Vertigo.DURATION * 2);
			Buff.affect(hero, MindVision.class, 1);
		}
	}
}
