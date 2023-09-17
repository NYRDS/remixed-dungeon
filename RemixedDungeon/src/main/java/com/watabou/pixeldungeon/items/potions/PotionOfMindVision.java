
package com.watabou.pixeldungeon.items.potions;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.MindVision;

public class PotionOfMindVision extends UpgradablePotion {

	{
		labelIndex = 10;
	}

	@Override
	protected void apply(Char hero ) {
		setKnown();
		Buff.affect( hero, MindVision.class, (float) (MindVision.DURATION * qualityFactor()));

		MindVision.reportMindVisionEffect();
	}

	@Override
	public String desc() {
        return StringsManager.getVar(R.string.PotionOfMindVision_Info);
    }

	@Override
	public int basePrice() {
		return 35;
	}
}
