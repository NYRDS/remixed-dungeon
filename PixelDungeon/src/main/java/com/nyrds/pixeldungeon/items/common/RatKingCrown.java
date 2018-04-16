package com.nyrds.pixeldungeon.items.common;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.items.rings.Artifact;
import com.watabou.pixeldungeon.items.rings.ArtifactBuff;
import com.watabou.pixeldungeon.ui.BuffIndicator;

public class RatKingCrown extends Artifact {

	public RatKingCrown() {
		imageFile = "items/artifacts.png";
		image = 17;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	protected Buff buff() {
		return new RatKingAuraBuff();
	}

	public class RatKingAuraBuff extends ArtifactBuff {
		@Override
		public int icon() {
			return BuffIndicator.RATTNESS;
		}

		@Override
		public String toString() {
			return Game.getVar(R.string.RatKingCrown_Buff);
		}
	}
}
