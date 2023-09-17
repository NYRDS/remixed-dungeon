
package com.watabou.pixeldungeon.items.quest;

import com.nyrds.pixeldungeon.mechanics.buffs.RageBuff;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.items.rings.Artifact;
import com.watabou.pixeldungeon.items.rings.ArtifactBuff;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

public class CorpseDust extends Artifact {

	public CorpseDust() {
		image = ItemSpriteSheet.DUST;

		setCursed(true);
		setCursedKnown(true);
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public ArtifactBuff buff() {
		return new RageBuff();
	}

	@Override
	public String info() {
        return super.info() + "\n\n" + StringsManager.getVar(R.string.CorpseDust_Info2);
	}

}
