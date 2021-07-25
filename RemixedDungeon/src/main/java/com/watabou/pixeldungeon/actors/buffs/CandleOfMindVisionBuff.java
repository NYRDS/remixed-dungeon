package com.watabou.pixeldungeon.actors.buffs;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;

public class CandleOfMindVisionBuff extends MindVision {

	public static final float DURATION = 100f;

	@Override
	public String name() {
		return StringsManager.getVar(R.string.MindVisionBuff_Name);
	}

	@Override
	public String desc() {
		return StringsManager.getVar(R.string.MindVisionBuff_Info);
	}
}
