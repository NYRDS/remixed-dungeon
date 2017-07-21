package com.nyrds.pixeldungeon.utils;

import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;

public class Position implements Bundlable {


	public int    cellId     = -1;
	public String levelId;

	@Deprecated
	private static final String LEVEL_DEPTH = "levelDepth";
	@Deprecated
	private static final String LEVEL_KIND  = "levelKind";

	private static final String LEVEL_ID    = "levelId";
	private static final String CELL_ID     = "cellId";

	public Position( String _levelId, int _cellId){
		levelId    = _levelId;
		cellId     = _cellId;
	}

	public Position(Position pos) {
		this(pos.levelId, pos.cellId);
	}

	public Position() {
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		int levelDepth = bundle.optInt(LEVEL_DEPTH, 1);
		String levelKind = bundle.optString(LEVEL_KIND, "SewerLevel");

		cellId     = bundle.getInt(CELL_ID);
		levelId    = bundle.optString(LEVEL_ID, DungeonGenerator.guessLevelId(levelKind, levelDepth));
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		bundle.put(LEVEL_ID,    levelId);
		bundle.put(CELL_ID,     cellId);
	}
	
	public boolean dontPack() {
		return false;
	}
}
