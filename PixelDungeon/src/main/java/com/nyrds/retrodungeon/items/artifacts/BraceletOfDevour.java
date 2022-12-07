package com.nyrds.retrodungeon.items.artifacts;

import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.buffs.Hunger;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.potions.PotionOfHealing;
import com.watabou.pixeldungeon.items.rings.Artifact;
import com.watabou.pixeldungeon.ui.BuffIndicator;

public class BraceletOfDevour extends Artifact {

	public BraceletOfDevour() {
		imageFile = "items/artifacts.png";
		image = 24;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	protected ArtifactBuff buff() {
		return new BraceletOfDevourBuff();
	}

	public class BraceletOfDevourBuff extends ArtifactBuff {
		@Override
		public int icon() {
			return BuffIndicator.DEVOUR;
		}

		@Override
		public String toString() {
			return Game.getVar(R.string.Devour_Buff);
		}
	}

	public static void Devour(Hero hero){
		hero.buff( Hunger.class ).satisfy( -(Hunger.STARVING/6) );
		PotionOfHealing.heal(hero, 0.16f);
	}

}
