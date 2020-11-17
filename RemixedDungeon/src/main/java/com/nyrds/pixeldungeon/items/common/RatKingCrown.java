package com.nyrds.pixeldungeon.items.common;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.actors.mobs.Rat;
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
	protected ArtifactBuff buff() {
		return new RatKingAuraBuff();
	}

	public static class RatKingAuraBuff extends ArtifactBuff {
		@Override
		public int icon() {
			return BuffIndicator.RATTNESS;
		}

		@Override
		public String name() {
			return Game.getVar(R.string.RatKingCrown_Buff);
		}

		@Override
		public int attackProc(Char attacker, Char defender, int damage) {
			if (defender instanceof Rat && attacker.buffLevel(getEntityKind())>0) {
				Mob.makePet((Rat)defender, attacker.getId());
			}
			return super.attackProc(attacker, defender, damage);
		}
	}
}
