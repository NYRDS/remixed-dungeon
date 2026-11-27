
package com.watabou.pixeldungeon.levels.painters;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.BlobFactory;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Room;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.utils.Point;
import com.watabou.utils.Random;
import lombok.SneakyThrows;

public class MagicWellPainter extends Painter {

	private static final String[] WATERS =
		{"WaterOfAwareness", "WaterOfHealth", "WaterOfTransmutation"};

	@SneakyThrows
	public static void paint( Level level, Room room ) {

		fill( level, room, Terrain.WALL );
		fill( level, room, 1, Terrain.EMPTY );

		Point c = room.center();
		set( level, c.x, c.y, Terrain.WELL );

		String waterKind =
			Dungeon.depth >= Dungeon.transmutation ?
			"WaterOfTransmutation" :
			Random.element( WATERS );

		if (waterKind.equals("WaterOfTransmutation")) {
			Dungeon.transmutation = Integer.MAX_VALUE;
		}

		Blob water = level.blobAt( waterKind );
		if (water == null) {
			water = BlobFactory.byKind(waterKind);
		}
		water.seed( c.x + level.getWidth() * c.y, 1 );
		level.blobPut( waterKind, water );

		room.entrance().set( Room.Door.Type.REGULAR );
	}
}
