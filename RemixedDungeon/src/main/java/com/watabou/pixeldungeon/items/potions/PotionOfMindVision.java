
package com.watabou.pixeldungeon.items.potions;
import com.nyrds.pixeldungeon.mechanics.buffs.BuffFactory;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.buffs.Buff;

public class PotionOfMindVision extends UpgradablePotion {

	{
		labelIndex = 10;
	}

	@Override
	protected void apply(Char hero ) {
		setKnown();
		Buff.affect( hero, BuffFactory.MIND_VISION, (float) (20f * qualityFactor()));

		CharUtils.reportMindVisionEffect();
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
