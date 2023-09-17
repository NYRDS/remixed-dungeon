
package com.watabou.pixeldungeon.actors.buffs;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.ui.BuffIndicator;
import com.watabou.pixeldungeon.utils.GLog;

public class MindVision extends FlavourBuff {

	public static final float DURATION = 20f;


	static public void reportMindVisionEffect() {
		Dungeon.observe();
		if (Dungeon.level.mobs.size() > 0) {
            GLog.i(StringsManager.getVar(R.string.PotionOfMindVision_Apply1));
		} else {
            GLog.i(StringsManager.getVar(R.string.PotionOfMindVision_Apply2));
		}
	}

	@Override
	public int icon() {
		return BuffIndicator.MIND_VISION;
	}

	@Override
	public void detach() {
		super.detach();
		Dungeon.observe();
	}

	@Override
	public void attachVisual() {
        target.showStatus(CharSprite.POSITIVE, StringsManager.getVar(R.string.Char_StaMind));
        target.showStatus(CharSprite.POSITIVE, StringsManager.getVar(R.string.Char_StaVision));
	}
}
