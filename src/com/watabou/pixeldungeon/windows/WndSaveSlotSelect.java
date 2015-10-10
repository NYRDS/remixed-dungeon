package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;

public class WndSaveSlotSelect extends WndOptions {
	
	WndSaveSlotSelect(boolean saving) {
		super(Game.getVar(R.string.WndSaveSlotSelect_SelectSlot), "", "1", "2", "3", "4", "5", "6", "7", "8", "9");
	}
	
}
