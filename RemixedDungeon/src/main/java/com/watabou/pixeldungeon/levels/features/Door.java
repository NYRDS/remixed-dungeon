
package com.watabou.pixeldungeon.levels.features;

import com.nyrds.platform.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
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

		if (level.getHeap( pos ) == null) {
			level.set( pos, Terrain.DOOR );
			GameScene.updateMapPair( pos );

			Dungeon.observe();
		}
	}
}
