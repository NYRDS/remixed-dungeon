package com.watabou.pixeldungeon.items.rings;

import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.hero.Hero;

import java.util.ArrayList;

public class UsableArtifact extends Artifact {

	protected static final String AC_USE = Game.getVar(R.string.SpiderCharm_Use);

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_USE );
		return actions;
	}
}
