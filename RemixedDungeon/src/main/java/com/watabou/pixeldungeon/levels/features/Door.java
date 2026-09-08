
package com.watabou.pixeldungeon.levels.features;

import com.nyrds.platform.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.scenes.GameScene;

public class Door {

	public static void enter( int pos ) {
		final Level level = Dungeon.level;
		level.set( pos, Terrain.OPEN_DOOR );
		GameScene.updateMapPair( pos );

		Dungeon.observe();
		
		if (Dungeon.isCellVisible(pos)) {
			Sample.INSTANCE.play( Assets.SND_OPEN );
		}
	}
	
	public static void leave( int pos ) {
		final Level level = Dungeon.level;

		// caveman: somebody stands in the doorway - leave it open
		if (level.getHeap( pos ) == null && Actor.findChar(pos) == null) {
			level.set( pos, Terrain.DOOR );
			GameScene.updateMapPair( pos );

			Dungeon.observe();
		}
	}

	// a heap keeps a door wedged open (leave() refuses while a heap sits on the
	// tile), so drops from a death in a doorway pick a nearby cell off the door
	public static int avoidDoor( int pos ) {
		final Level level = Dungeon.level;

		int tile = level.map[pos];
		if (tile != Terrain.DOOR && tile != Terrain.OPEN_DOOR) {
			return pos;
		}

		int fallback = Level.INVALID_CELL;
		for (int n : Level.NEIGHBOURS8) {
			int p = n + pos;
			if (!level.cellValid(p)
				|| level.map[p] == Terrain.DOOR
				|| level.map[p] == Terrain.OPEN_DOOR
				|| !(level.avoid[p] || level.passable[p])) {
				continue;
			}
			if (Actor.findChar(p) == null) {
				return p;
			}
			if (fallback == Level.INVALID_CELL) {
				fallback = p;
			}
		}

		return fallback != Level.INVALID_CELL ? fallback : pos;
	}
}
