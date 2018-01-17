package com.nyrds.pixeldungeon.items.books;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.CommonActions;
import com.watabou.pixeldungeon.actors.buffs.Blindness;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.utils.GLog;

import java.util.ArrayList;

abstract public class Book extends Item {

	public static final float TIME_TO_READ	= 2f;

	{
		imageFile = "items/books.png";
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( CommonActions.AC_READ );
		return actions;
	}


	@Override
	public void execute( Hero hero, String action ) {
		if (action.equals( CommonActions.AC_READ )) {

			if (hero.buff( Blindness.class ) != null) {
				GLog.w(Game.getVar(R.string.Codex_Blinded));
			} else {
				setCurUser(hero);

				doRead(hero);
			}

		} else {

			super.execute( hero, action );

		}
	}

	abstract protected void doRead(Hero hero);

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}
}
