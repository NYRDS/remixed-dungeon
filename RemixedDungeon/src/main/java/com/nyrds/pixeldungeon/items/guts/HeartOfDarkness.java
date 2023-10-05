package com.nyrds.pixeldungeon.items.guts;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.guts.SpiritOfPain;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.rings.Artifact;
import com.watabou.pixeldungeon.items.rings.ArtifactBuff;
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
	public ArtifactBuff buff() {
		return new HeartOfDarknessBuff();
	}

	public static class HeartOfDarknessBuff extends ArtifactBuff {
		@Override
		public int icon() {
			return BuffIndicator.DARKVEIL;
		}

		@Override
		public String name() {
            return StringsManager.getVar(R.string.DarkVeilBuff_Name);
        }

		@Override
		public String desc() {
            return StringsManager.getVar(R.string.DarkVeilBuff_Info);
        }

		@Override
		public int defenceProc(Char defender, Char enemy, int damage) {
			int defenderPos = defender.getPos();
			int spiritPos = defender.level().getEmptyCellNextTo(defenderPos);

			if (defender.level().cellValid(spiritPos)) {
				SpiritOfPain spirit = new SpiritOfPain();
				spirit.setPos(spiritPos);
				Mob.makePet(spirit, defender.getId());
				defender.level().spawnMob(spirit, 0, defenderPos);
			}
			return damage;
		}
	}
}
