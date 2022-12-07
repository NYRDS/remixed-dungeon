package com.nyrds.retrodungeon.utils;

import com.watabou.pixeldungeon.levels.Level;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;

public class Position implements Bundlable {

	public int cellId = -1;
	public String levelId;

	public int x = -1;
	public int y = -1;

	@Deprecated
	private static final String LEVEL_DEPTH = "levelDepth";
	@Deprecated
	private static final String LEVEL_KIND  = "levelKind";

	private static final String LEVEL_ID = "levelId";
	private static final String CELL_ID  = "cellId";

	private static final String X = "x";
	private static final String Y = "y";

	public Position(String _levelId, int _cellId) {
		levelId = _levelId;
		cellId = _cellId;
	}

	public Position(String _levelId, int _x, int _y) {
		levelId = _levelId;
		x = _x;
		y = _y;
	}

	public Position(Position pos) {
		levelId = pos.levelId;
		cellId = pos.cellId;
		x = pos.x;
		y = pos.y;
	}

	public Position() {
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		int levelDepth = bundle.optInt(LEVEL_DEPTH, 1);
		String levelKind = bundle.optString(LEVEL_KIND, "SewerLevel");

		cellId = bundle.optInt(CELL_ID, -1);

		x = bundle.optInt(X, -1);
		y = bundle.optInt(Y, -1);

		levelId = bundle.optString(LEVEL_ID, DungeonGenerator.guessLevelId(levelKind, levelDepth));
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		bundle.put(LEVEL_ID, levelId);

		if (cellId >= 0) {
			bundle.put(CELL_ID, cellId);
		}

		if (x >= 0 && y >= 0) {
			bundle.put(X, x);
			bundle.put(Y, y);
		}
	}

	public boolean dontPack() {
		return false;
	}

	public void computeCell(Level level) {
		if(x>=0&&y>=0) {
			cellId = x + y*level.getWidth();
		}
	}
}
