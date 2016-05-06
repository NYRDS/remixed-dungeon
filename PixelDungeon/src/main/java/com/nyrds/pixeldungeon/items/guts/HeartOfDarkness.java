package com.nyrds.pixeldungeon.items.guts;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.items.rings.Artifact;
import com.watabou.pixeldungeon.ui.BuffIndicator;

public class HeartOfDarkness extends Artifact {

	public HeartOfDarkness() {
		imageFile = "items/artifacts.png";
		image = 18;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	protected Artifact.ArtifactBuff buff() {
		return new HeartOfDarknessBuff();
	}

	public class HeartOfDarknessBuff extends Artifact.ArtifactBuff {
		@Override
		public int icon() {
			return BuffIndicator.DARKVEIL;
		}

		@Override
		public String toString() {
			return Game.getVar(R.string.DarkVeil_Buff);
		}
	}
/*
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
	}*/
}
