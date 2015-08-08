package com.nyrds.pixeldungeon.utils;

import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;

public class Position implements Bundlable {

	public int    levelDepth = -1;
	public int    cellId     = -1;
	public String levelKind  = "DeadEndLevel";
	public int    xs         = 32;
	public int    ys         = 32;
	
	static final String LEVEL_DEPTH = "levelDepth";
	static final String LEVEL_KIND  = "levelKind";
	static final String CELL_ID     = "cellId";

	public Position(String _levelKind, int _levelDepth, int _cellId){
		levelKind  = _levelKind;
		levelDepth = _levelDepth;
		cellId     = _cellId;
	}
	
	public Position() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		levelDepth = bundle.getInt(LEVEL_DEPTH);
		levelKind  = bundle.getString(LEVEL_KIND);
		cellId     = bundle.getInt(CELL_ID);
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		bundle.put(LEVEL_DEPTH, levelDepth);
		bundle.put(CELL_ID,     cellId);
		bundle.put(LEVEL_KIND,  levelKind);
	}
	
	public boolean dontPack() {
		return false;
	}
	
}
