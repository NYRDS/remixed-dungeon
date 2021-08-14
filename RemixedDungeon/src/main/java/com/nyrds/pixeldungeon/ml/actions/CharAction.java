package com.nyrds.pixeldungeon.ml.actions;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.levels.Level;

public abstract class CharAction {
	
	public int dst;

	abstract public boolean act(Char hero);

	@Override
	public String toString() {
		Level level = Dungeon.level;

		if (level==null) {
			return   getClass().getSimpleName() +
					"{dst=" + dst +
					'}';
		}

		if(!level.cellValid(dst)) {
			return   getClass().getSimpleName() +
					"{dst=" + dst +
					'}';
		}

		return   getClass().getSimpleName() +
				"{dst=" + dst +
				  "("+ level.tileNameByCell(dst) + "," +
				(Actor.findChar(dst) != null ? Actor.findChar(dst).getEntityKind() : "") + "," +
				(level.getTopLevelObject(dst) != null ? level.getTopLevelObject(dst).getEntityKind() : "") +
				(level.getHeap(dst) != null ? level.getHeap(dst).peek().getEntityKind() : "") + ")" +
				'}';
	}
}
