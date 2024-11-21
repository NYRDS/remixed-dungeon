
package com.watabou.pixeldungeon.levels;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.noosa.Scene;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.mobs.npcs.Imp;
import com.watabou.pixeldungeon.levels.Room.Type;
import com.nyrds.util.Graph;
import com.nyrds.util.Random;

import java.util.List;

public class LastShopLevel extends RegularLevel {
	
	{
		color1 = 0x4b6636;
		color2 = 0xf2f2f2;
	}

	@Override
	protected String tilesTexXyz() {
		return Assets.TILES_CITY_XYZ;
	}

	@Override
	public String tilesTex() {
		return Assets.TILES_CITY;
	}
	
	@Override
	public String waterTex() {
		return Assets.WATER_CITY;
	}
	
	@Override
	protected boolean build() {
		
		initRooms();
		
		int distance;
		int retry = 0;
		int minDistance = (int)Math.sqrt( rooms.size() );
		do {
			int innerRetry = 0;
			do {
				if (innerRetry++ > 10) {
					return false;
				}
				roomEntrance = Random.element( rooms );
			} while (roomEntrance.width() < 4 || roomEntrance.height() < 4);
			
			innerRetry = 0;
			do {
				if (innerRetry++ > 10) {
					return false;
				}
				setRoomExit(Random.element( rooms ));
			} while (getRoomExit() == roomEntrance || getRoomExit().width() < 6 || getRoomExit().height() < 6 || getRoomExit().top == 0);
	
			Graph.buildDistanceMap( rooms, getRoomExit());
			distance = Graph.buildPath(roomEntrance, getRoomExit()).size();
			
			if (retry++ > 10) {
				return false;
			}
			
		} while (distance < minDistance);
		
		roomEntrance.type = Type.ENTRANCE;
		getRoomExit().type = Type.EXIT;
		
		Graph.buildDistanceMap( rooms, getRoomExit());
		List<Room> path = Graph.buildPath(roomEntrance, getRoomExit());
		
		Graph.setPrice( path, roomEntrance.distance );
		
		Graph.buildDistanceMap( rooms, getRoomExit());
		path = Graph.buildPath(roomEntrance, getRoomExit());
		
		Room room = roomEntrance;
		for (Room next : path) {
			room.connect( next );
			room = next;
		}
		
		Room roomShop = null;
		int shopSquare = 0;
		for (Room r : rooms) {
			if (r.type == Type.NULL && !r.connected.isEmpty()) {
				r.type = Type.PASSAGE; 
				if (r.square() > shopSquare) {
					roomShop = r;
					shopSquare = r.square();
				}
			}
		}
		
		if (roomShop == null || shopSquare < 30) {
			return false;
		} else {
			roomShop.type = Imp.Quest.isCompleted() ? Room.Type.SHOP : Room.Type.STANDARD;
		}
		
		paint();
		
		paintWater();
		paintGrass();
		
		return true;
	}
	
	@Override
	protected void decorate() {	
		
		for (int i=0; i < getLength(); i++) {
			if (map[i] == Terrain.EMPTY && Random.Int( 10 ) == 0) { 
				
				map[i] = Terrain.EMPTY_DECO;
				
			} else if (map[i] == Terrain.WALL && Random.Int( 8 ) == 0) {
				
				map[i] = Terrain.WALL_DECO;
				
			} else if (map[i] == Terrain.SECRET_DOOR) {
				
				map[i] = Terrain.DOOR;
				
			}
		}
		
		if (Imp.Quest.isCompleted()) {
			placeEntranceSign();
		}
	}
	
	@Override
	protected void createMobs() {	
	}
	
	public Actor respawner() {
		return null;
	}
	
	@Override
	protected void createItems() {
		dropBones();
	}
	
	@Override
	public int randomRespawnCell() {
		return -1;
	}
	
	@Override
	public String tileName( int tile ) {
		switch (tile) {
		case Terrain.WATER:
            return StringsManager.getVar(R.string.LastShopLevel_TileWater);
            case Terrain.HIGH_GRASS:
                return StringsManager.getVar(R.string.LastShopLevel_TileHighGrass);
            default:
			return super.tileName( tile );
		}
	}
	
	@Override
	public String tileDesc(int tile) {
		switch (tile) {
		case Terrain.ENTRANCE:
            return StringsManager.getVar(R.string.LastShopLevel_TileDescEntrance);
            case Terrain.EXIT:
                return StringsManager.getVar(R.string.LastShopLevel_TileDescExit);
            case Terrain.WALL_DECO:
		case Terrain.EMPTY_DECO:
            return StringsManager.getVar(R.string.LastShopLevel_TileDescDeco);
            case Terrain.EMPTY_SP:
                return StringsManager.getVar(R.string.LastShopLevel_TileDescEmptySP);
            default:
			return super.tileDesc( tile );
		}
	}

	@Override
	protected boolean[] water() {
		return Patch.generate(this, 0.35f, 4 );
	}

	@Override
	protected boolean[] grass() {
		return Patch.generate(this, 0.30f, 3 );
	}
	
	@Override
	public void addVisuals( Scene scene ) {
		CityLevel.addVisuals( this, scene );
	}
}
