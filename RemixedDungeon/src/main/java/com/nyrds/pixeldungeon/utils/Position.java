package com.nyrds.pixeldungeon.utils;

import androidx.annotation.Keep;

import com.nyrds.Packable;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;

public class Position implements Bundlable {

	@Packable
	public int cellId = -1;
	@Packable
	public String levelId = DungeonGenerator.getEntryLevel();

	@Packable
	public int x = -1;

	@Packable
	public int y = -1;

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

	@Keep
	public Position() {
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
	}

	@Override
	public void storeInBundle(Bundle bundle) {
	}

	public boolean dontPack() {
		return false;
	}

	public void computeCell(Level level) {
		if(x>=0&&y>=0) {
			cellId = x + y*level.getWidth();
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Position position = (Position) o;

		if (cellId != position.cellId) return false;
		if (x != position.x) return false;
		if (y != position.y) return false;
		return levelId.equals(position.levelId);
	}

	@Override
	public int hashCode() {
		int result = cellId;
		result = 31 * result + (levelId != null ? levelId.hashCode() : 0);
		result = 31 * result + x;
		result = 31 * result + y;
		return result;
	}
}
