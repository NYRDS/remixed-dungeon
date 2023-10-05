
package com.watabou.pixeldungeon.levels.painters;

import com.watabou.pixeldungeon.actors.blobs.Foliage;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Room;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.plants.Moongrace;
import com.watabou.pixeldungeon.plants.Sungrass;
import com.watabou.utils.Random;

public class GardenPainter extends Painter {

	public static void paint( Level level, Room room ) {
		
		fill( level, room, Terrain.WALL );
		fill( level, room, 1, Terrain.HIGH_GRASS );
		fill( level, room, 2, Terrain.GRASS );
		
		room.entrance().set( Room.Door.Type.REGULAR );
		
		int bushes = Random.Int( 3 ) == 0 ? (Random.Int( 5 ) == 0 ? 2 : 1) : 0;
		for (int i=0; i < bushes; i++) {
			int cellToPlant = room.random(level);
			
			if(level.getTopLevelObject(cellToPlant)==null) {
				level.plant( new Sungrass.Seed(), cellToPlant );
			}

			cellToPlant = room.random(level);
			if(level.getTopLevelObject(cellToPlant)==null) {
				level.plant( new Moongrace.Seed(), cellToPlant );
			}
		}
		
		Foliage light = (Foliage)level.blobs.get( Foliage.class );
		if (light == null) {
			light = new Foliage();
		}

		for (int i=room.top + 1; i < room.bottom; i++) {
			for (int j=room.left + 1; j < room.right; j++) {
				light.seed( j + level.getWidth() * i, 1 );
			}
		}
		level.blobs.put( Foliage.class, light );
	}
}
