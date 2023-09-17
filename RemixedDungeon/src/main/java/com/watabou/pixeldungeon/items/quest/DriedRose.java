
package com.watabou.pixeldungeon.items.quest;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.items.rings.Artifact;
import com.watabou.pixeldungeon.items.rings.ArtifactBuff;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.ui.BuffIndicator;

public class DriedRose extends Artifact {

	public DriedRose() {
		image = ItemSpriteSheet.ROSE;
	}

	@Override
	public ArtifactBuff buff() {
		if (!isCursed()) {
			return new OneWayLoveBuff();
		} else {
			return new OneWayCursedLoveBuff();
		}
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public String info() {
        return super.info() + "\n\n" + StringsManager.getVar(R.string.DriedRose_Info2);
	}

	public static class OneWayLoveBuff extends ArtifactBuff {
		@Override
		public int icon() {
			return BuffIndicator.ROSE;
		}

		@Override
		public String name() {
            return StringsManager.getVar(R.string.DriedRoseBuff_Name);
        }

		@Override
		public String desc() {
            return StringsManager.getVar(R.string.DriedRoseBuff_Info);
        }
	}

	public static class OneWayCursedLoveBuff extends ArtifactBuff {
		@Override
		public int icon() {
			return BuffIndicator.CURSED_ROSE;
		}

		@Override
		public String name() {
            return StringsManager.getVar(R.string.DriedRoseCursedBuff_Name);
        }

		@Override
		public String desc() {
            return StringsManager.getVar(R.string.DriedRoseCursedBuff_Info);
        }
	}
}
