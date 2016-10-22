package com.nyrds.pixeldungeon.items.artifacts;

import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.MindVision;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.rings.UsableArtifact;

import java.util.ArrayList;

import static com.watabou.pixeldungeon.items.potions.PotionOfMindVision.reportMindVisionEffect;

public class CandleOfMindVision extends UsableArtifact {

	private static final String AC_DIM = "Dim";
	private static final String AC_LIT = "Lit";

	int charges;

	public CandleOfMindVision() {
		imageFile = "items/candle.png";
		image = 0;
	}


	@Override
	public ArrayList<String> actions(Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_LIT );
		actions.add( AC_DIM );
		return actions;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public void execute(final Hero ch, String action ) {
		setCurUser(ch);

		if (action.equals( AC_LIT )) {
			Buff.affect( ch, MindVision.class, charges );
			charges = 0;
			reportMindVisionEffect();
			return;
		}

		if (action.equals( AC_DIM )) {
			Buff.detach(ch, MindVision.class);
			return;
		}
		super.execute( ch, action );
	}

}
