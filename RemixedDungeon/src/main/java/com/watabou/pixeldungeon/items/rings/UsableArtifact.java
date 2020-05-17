package com.watabou.pixeldungeon.items.rings;

import com.watabou.pixeldungeon.actors.Char;

import java.util.ArrayList;

public class UsableArtifact extends Artifact {

	protected static final String AC_USE = "SpiderCharm_Use";

	@Override
	public ArrayList<String> actions(Char hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_USE );
		return actions;
	}
}
