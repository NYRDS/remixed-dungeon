
package com.watabou.pixeldungeon.levels.painters;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.blobs.WaterOfAwareness;
import com.watabou.pixeldungeon.actors.blobs.WaterOfHealth;
import com.watabou.pixeldungeon.actors.blobs.WaterOfTransmutation;
import com.watabou.pixeldungeon.actors.blobs.WellWater;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Room;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

import lombok.SneakyThrows;

public class MagicWellPainter extends Painter {

	private static final Class<?>[] WATERS = 
		{WaterOfAwareness.class, WaterOfHealth.class, WaterOfTransmutation.class};

	@SneakyThrows
	public static void paint( Level level, Room room ) {

		fill( level, room, Terrain.WALL );
		fill( level, room, 1, Terrain.EMPTY );
		
		Point c = room.center();
		set( level, c.x, c.y, Terrain.WELL );
		
		@SuppressWarnings("unchecked")
		Class<? extends WellWater> waterClass = 
			Dungeon.depth >= Dungeon.transmutation ?
			WaterOfTransmutation.class :		
			(Class<? extends WellWater>)Random.element( WATERS );
			
		if (waterClass == WaterOfTransmutation.class) {
			Dungeon.transmutation = Integer.MAX_VALUE;
		}
		
		WellWater water = (WellWater)level.blobs.get( waterClass );
		if (water == null) {
			water = waterClass.newInstance();
		}
		water.seed( c.x + level.getWidth() * c.y, 1 );
		level.blobs.put( waterClass, water );
		
		room.entrance().set( Room.Door.Type.REGULAR );
	}
}
