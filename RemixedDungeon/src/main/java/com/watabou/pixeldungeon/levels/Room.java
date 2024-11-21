
package com.watabou.pixeldungeon.levels;

import com.nyrds.platform.events.EventCollector;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.levels.painters.ArmoryPainter;
import com.watabou.pixeldungeon.levels.painters.BlacksmithPainter;
import com.watabou.pixeldungeon.levels.painters.CryptPainter;
import com.watabou.pixeldungeon.levels.painters.EntrancePainter;
import com.watabou.pixeldungeon.levels.painters.ExitPainter;
import com.watabou.pixeldungeon.levels.painters.GardenPainter;
import com.watabou.pixeldungeon.levels.painters.LaboratoryPainter;
import com.watabou.pixeldungeon.levels.painters.LibraryPainter;
import com.watabou.pixeldungeon.levels.painters.MagicWellPainter;
import com.watabou.pixeldungeon.levels.painters.Painter;
import com.watabou.pixeldungeon.levels.painters.PassagePainter;
import com.watabou.pixeldungeon.levels.painters.PitPainter;
import com.watabou.pixeldungeon.levels.painters.PoolPainter;
import com.watabou.pixeldungeon.levels.painters.PrisonBossExitPainter;
import com.watabou.pixeldungeon.levels.painters.RatKingPainter;
import com.watabou.pixeldungeon.levels.painters.SewerBossExitPainter;
import com.watabou.pixeldungeon.levels.painters.ShopPainter;
import com.watabou.pixeldungeon.levels.painters.StandardPainter;
import com.watabou.pixeldungeon.levels.painters.StatuePainter;
import com.watabou.pixeldungeon.levels.painters.StoragePainter;
import com.watabou.pixeldungeon.levels.painters.TrapsPainter;
import com.watabou.pixeldungeon.levels.painters.TreasuryPainter;
import com.watabou.pixeldungeon.levels.painters.TunnelPainter;
import com.watabou.pixeldungeon.levels.painters.VaultPainter;
import com.watabou.pixeldungeon.levels.painters.WarehousePainter;
import com.watabou.pixeldungeon.levels.painters.WeakFloorPainter;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.nyrds.util.Graph;
import com.nyrds.util.Point;
import com.nyrds.util.Random;
import com.nyrds.util.Rect;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import lombok.SneakyThrows;

public class Room extends Rect implements Graph.Node, Bundlable {
	
	public final HashSet<Room>       neighbours = new HashSet<>();
	public final HashMap<Room, Door> connected  = new HashMap<>();
	
	public int distance;
	public int price = 1;

	public enum Type {
		NULL( null ),
		STANDARD	( StandardPainter.class ),
		ENTRANCE	( EntrancePainter.class ),
		EXIT		( ExitPainter.class ),
		SEWER_BOSS_EXIT	( SewerBossExitPainter.class ),
		PRISON_BOSS_EXIT( PrisonBossExitPainter.class ),
		TUNNEL		( TunnelPainter.class ),
		PASSAGE		( PassagePainter.class ),
		SHOP		( ShopPainter.class ),
		BLACKSMITH	( BlacksmithPainter.class ),
		TREASURY	( TreasuryPainter.class ),
		ARMORY		( ArmoryPainter.class ),
		LIBRARY		( LibraryPainter.class ),
		LABORATORY	( LaboratoryPainter.class ),
		VAULT		( VaultPainter.class ),
		TRAPS		( TrapsPainter.class ),
		STORAGE		( StoragePainter.class ),
		MAGIC_WELL	( MagicWellPainter.class ),
		GARDEN		( GardenPainter.class ),
		CRYPT		( CryptPainter.class ),
		STATUE		( StatuePainter.class ),
		POOL		( PoolPainter.class ),
		RAT_KING	( RatKingPainter.class ),
		WEAK_FLOOR	( WeakFloorPainter.class ),
		PIT			( PitPainter.class ),
		WAREHOUSE	( WarehousePainter.class );
		
		private Method paint;
		
		@SneakyThrows
		Type(Class<? extends Painter> painter) {
			if(painter==null) {
				return;
			}

			paint = painter.getMethod( "paint", Level.class, Room.class );
		}

		@SneakyThrows
		public void paint( Level level, Room room ) {
			if(paint==null){
				EventCollector.logException("no painter for " + this.name());
				return;
			}

			paint.invoke( null, level, room );
		}
	}

	public static final ArrayList<Type> SPECIALS = new ArrayList<>(Arrays.asList(
			Type.WEAK_FLOOR,
			Type.MAGIC_WELL,
			Type.CRYPT,
			Type.POOL,
			Type.GARDEN,
			Type.LIBRARY,
			Type.ARMORY,
			Type.TREASURY,
			Type.TRAPS,
			Type.STORAGE,
			Type.STATUE,
			Type.LABORATORY,
			Type.VAULT,
			Type.WAREHOUSE
	));

	public Type type = Type.NULL;
	
	public int random(Level level) {
		return random(level, 0 );
	}
	
	public int random(Level level, int m ) {
		int x = Random.Int( left + 1 + m, right - m );
		int y = Random.Int( top + 1 + m, bottom - m );
		return level.cell(x, y);
	}
	
	public void addNeighbor(Room other ) {
		
		Rect i = intersect( other );
		if ((i.width() == 0 && i.height() >= 3) || 
			(i.height() == 0 && i.width() >= 3)) {
			neighbours.add( other );
			other.neighbours.add( this );
		}
		
	}
	
	public void connect( Room room ) {
		if (!connected.containsKey( room )) {	
			connected.put( room, null );
			room.connected.put( this, null );			
		}
	}

	protected boolean isRoomIsolatedFrom(Room tgt) {

		HashSet<Room> checkedRooms = new HashSet<>();
		ArrayList<Room> uncheckedRooms = new ArrayList<>();

		checkedRooms.add(this);

		for (Room roomToCheck : edges()) {
			if (connected.containsKey(roomToCheck)) {
				uncheckedRooms.add(roomToCheck);
			}
		}

		while (!uncheckedRooms.isEmpty()) {
			Room currentRoom = uncheckedRooms.remove(uncheckedRooms.size()-1);

			if (currentRoom == tgt) {
				return false;
			}

			checkedRooms.add(currentRoom);

			for (Room roomToCheck : currentRoom.edges()) {
				if(checkedRooms.contains(roomToCheck)) {
					continue;
				}

				if (currentRoom.connected.containsKey(roomToCheck)) {
					uncheckedRooms.add(roomToCheck);
				}
			}
		}
		return true;
	}

	public Door entrance() {
		return connected.values().iterator().next();
	}
	
	public boolean inside( int p ) {
		int x = p % Dungeon.level.getWidth();
		int y = p / Dungeon.level.getWidth();
		return x > left && y > top && x < right && y < bottom;
	}
	
	public Point center() {
		return new Point( 
			(left + right) / 2 + (((right - left) & 1) == 1 ? Random.Int( 2 ) : 0),
			(top + bottom) / 2 + (((bottom - top) & 1) == 1 ? Random.Int( 2 ) : 0) );
	}
	
	// **** Graph.Node interface ****

	@Override
	public int distance() {
		return distance;
	}

	@Override
	public void distance( int value ) {
		distance = value;
	}
	
	@Override
	public int price() {
		return price;
	}

	@Override
	public void price( int value ) {
		price = value;
	}

	@Override
	public Collection<Room> edges() {
		return neighbours;
	} 
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		bundle.put( "type",   type.toString() );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		type   = bundle.getEnum("type", Type.class, Type.STANDARD);
	}
	
	public boolean dontPack() {
		return false;
	}
	
	public static void shuffleTypes() {
		Collections.shuffle(SPECIALS);
	}
	
	public static void useType( Type type ) {
		if (SPECIALS.remove( type )) {
			SPECIALS.add( type );
		}
	}
	
	private static final String ROOMS	= "rooms";
	
	public static void restoreRoomsFromBundle( Bundle bundle ) {
		if (bundle.contains( ROOMS )) {
			SPECIALS.clear();
			for (String type : bundle.getStringArray( ROOMS )) {
				SPECIALS.add( Type.valueOf( type ));
			}
		} else {
			shuffleTypes();
		}
	}
	
	public static void storeRoomsInBundle( Bundle bundle ) {
		String[] array = new String[SPECIALS.size()];
		for (int i=0; i < array.length; i++) {
			array[i] = SPECIALS.get( i ).toString();
		}
		bundle.put( ROOMS, array );
	}
	
	public static class Door extends Point {
		
		public enum Type {
			EMPTY, TUNNEL, REGULAR, UNLOCKED, HIDDEN, BARRICADE, LOCKED
		}
		public Type type = Type.EMPTY;
		
		public Door( int x, int y ) {
			super( x, y );
		}
		
		public void set( Type type ) {
			if (type.compareTo( this.type ) > 0) {
				this.type = type;
			}
		}
	}
}














