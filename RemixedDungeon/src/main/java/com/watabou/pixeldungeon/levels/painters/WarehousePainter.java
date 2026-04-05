
package com.watabou.pixeldungeon.levels.painters;

import com.nyrds.pixeldungeon.items.Treasury;
import com.nyrds.pixeldungeon.levels.objects.Barrel;
import com.nyrds.pixeldungeon.levels.objects.LevelObjectsFactory;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Room;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

public class WarehousePainter extends Painter {

	public static void paint( Level level, Room room ) {

		fill( level, room, Terrain.WALL );
		fill( level, room, 1, Terrain.EMPTY_SP );
		
		Room.Door entrance = room.entrance();
		entrance.set( Room.Door.Type.HIDDEN );
		
		paintSokobanPuzzle(level, room);
	}
	
	private static void paintSokobanPuzzle(Level level, Room room) {
		int width = room.width();
		int height = room.height();
		
		if (width < 7 || height < 7) {
			paintSimplePuzzle(level, room);
			return;
		}
		
		Point center = new Point((room.left + room.right) / 2, (room.top + room.bottom) / 2);
		
		set( level, center.x - 1, center.y - 1, Terrain.WALL );
		set( level, center.x, center.y - 1, Terrain.WALL );
		set( level, center.x + 1, center.y, Terrain.WALL );
		set( level, center.x, center.y + 1, Terrain.WALL );
		
		int[][] barrelPositions = {
			{room.left + 2, room.top + 2},
			{room.right - 2, room.top + 2},
			{room.left + 2, room.bottom - 2}
		};
		
		int[][] targetPositions = {
			{room.right - 2, room.bottom - 2},
			{room.left + 2, room.bottom - 2},
			{room.right - 2, room.top + 2}
		};
		
		for (int[] pos : barrelPositions) {
			if (isInsideRoom(room, pos[0], pos[1]) && !isWall(level, pos[0], pos[1])) {
				level.addLevelObject(new Barrel(level.cell(pos[0], pos[1])));
			}
		}
		
		for (int[] pos : targetPositions) {
			if (isInsideRoom(room, pos[0], pos[1]) && !isWall(level, pos[0], pos[1])) {
				int cell = level.cell(pos[0], pos[1]);
				level.putLevelObject(LevelObjectsFactory.PEDESTAL, cell);
				
				Item prize = Random.oneOf(
					Treasury.getLevelTreasury().random(Treasury.Category.BULLETS),
					Treasury.getLevelTreasury().random(Treasury.Category.THROWABLE)
				);
				level.drop(prize, cell, Heap.Type.CHEST);
			}
		}
	}
	
	private static void paintSimplePuzzle(Level level, Room room) {
		int cx = (room.left + room.right) / 2;
		int cy = (room.top + room.bottom) / 2;
		
		set(level, cx, cy, Terrain.WALL);
		
		int barrelCount = Math.min(3, (room.width() * room.height()) / 10);
		
		for (int i = 0; i < barrelCount; i++) {
			int x, y;
			int attempts = 0;
			do {
				x = Random.IntRange(room.left + 1, room.right - 1);
				y = Random.IntRange(room.top + 1, room.bottom - 1);
				attempts++;
			} while (attempts < 50 && (level.map[level.cell(x, y)] != Terrain.EMPTY_SP || level.getLevelObject(level.cell(x, y)) != null));
			
			if (attempts < 50) {
				level.addLevelObject(new Barrel(level.cell(x, y)));
			}
		}
		
		for (int i = 0; i < barrelCount; i++) {
			int x, y;
			int attempts = 0;
			do {
				x = Random.IntRange(room.left + 1, room.right - 1);
				y = Random.IntRange(room.top + 1, room.bottom - 1);
				attempts++;
			} while (attempts < 50 && (level.map[level.cell(x, y)] != Terrain.EMPTY_SP || 
					level.getHeap(level.cell(x, y)) != null || 
					level.getLevelObject(level.cell(x, y)) != null));
			
			if (attempts < 50) {
				int cell = level.cell(x, y);
				level.putLevelObject(LevelObjectsFactory.PEDESTAL, cell);
				
				Item prize = Random.oneOf(
					Treasury.getLevelTreasury().random(Treasury.Category.BULLETS),
					Treasury.getLevelTreasury().random(Treasury.Category.THROWABLE)
				);
				level.drop(prize, cell, Heap.Type.CHEST);
			}
		}
	}
	
	private static boolean isInsideRoom(Room room, int x, int y) {
		return x > room.left && x < room.right && y > room.top && y < room.bottom;
	}
	
	private static boolean isWall(Level level, int x, int y) {
		return level.map[level.cell(x, y)] == Terrain.WALL;
	}
}
