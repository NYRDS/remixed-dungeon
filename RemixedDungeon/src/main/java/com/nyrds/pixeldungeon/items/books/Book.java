package com.nyrds.pixeldungeon.items.books;

import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Blindness;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.utils.GLog;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

abstract public class Book extends Item {

	public static final float TIME_TO_READ	= 2f;

	{
		imageFile = "items/books.png";
	}

	@Override
	public ArrayList<String> actions(Char hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( CommonActions.AC_READ );
		return actions;
	}


	@Override
	public void _execute(@NotNull Char chr, @NotNull String action ) {
		if (action.equals( CommonActions.AC_READ )) {

			if (chr.hasBuff( Blindness.class )) {
                GLog.w(StringsManager.getVar(R.string.Codex_Blinded));
			} else {
				doRead(chr);
			}

		} else {

			super._execute(chr, action );

		}
	}

	abstract protected void doRead(Char hero);

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}
}
