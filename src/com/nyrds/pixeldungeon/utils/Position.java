package com.nyrds.pixeldungeon.utils;

import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;

public class Position implements Bundlable {

	public int levelId = -1;
	public int cellId  = -1;
	
	
	static final String LEVEL_ID = "levelId";
	static final String CELL_ID  = "cellId";
	
	@Override
	public void restoreFromBundle(Bundle bundle) {
		levelId = bundle.getInt(LEVEL_ID);
		cellId  = bundle.getInt(CELL_ID);
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		bundle.put(LEVEL_ID, levelId);
		bundle.put(CELL_ID, cellId);
	}
	
}
