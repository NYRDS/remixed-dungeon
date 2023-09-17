
package com.watabou.pixeldungeon.items.quest;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.items.rings.Artifact;
import com.watabou.pixeldungeon.items.rings.ArtifactBuff;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.ui.BuffIndicator;

public class RatSkull extends Artifact {

	public RatSkull() {
		image = ItemSpriteSheet.SKULL;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public ArtifactBuff buff() {
		return new RatterAura();
	}

	@Override
	public String info() {
        return super.info() + "\n\n" + StringsManager.getVar(R.string.RatSkull_Info2);
	}

	public static class RatterAura extends ArtifactBuff {

		@Override
		public String name() {
			return StringsManager.getVar(R.string.RatSkullBuff_Name);
		}

		@Override
		public String desc() {
			return StringsManager.getVar(R.string.RatSkullBuff_Info);
		}

		@Override
		public int icon() {
			return BuffIndicator.RAT_SKULL;
		}
	}

	@Override
	public int price() {
		return 100;
	}
}
