
package com.watabou.pixeldungeon.levels.painters;

import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Room;
import com.watabou.utils.Point;

import java.util.ArrayList;
import java.util.Collections;

public class PassagePainter extends Painter {

	public static void paint( Level level, Room room ) {
		
		pasWidth = room.width() - 2;
		pasHeight = room.height() - 2;
		
		int floor = level.tunnelTile();
		
		ArrayList<Integer> joints = new ArrayList<>();
		for (Point door : room.connected.values()) {
			joints.add( xy2p( room, door ) );
		}
		Collections.sort( joints );
		
		int nJoints = joints.size();
		int perimeter = pasWidth * 2 + pasHeight * 2;
		
		int start = 0;
		int maxD = joints.get( 0 ) + perimeter - joints.get( nJoints - 1 );
		for (int i=1; i < nJoints; i++) {
			int d = joints.get( i ) - joints.get( i - 1 );
			if (d > maxD) {
				maxD = d;
				start = i;
			}
		}
		
		int end = (start + nJoints - 1) % nJoints;
		
		int p = joints.get( start );
		do {
			set( level, p2xy( room, p ), floor );
			p = (p + 1) % perimeter;
		} while (p != joints.get( end ));
		
		set( level, p2xy( room, p ), floor );
		
		for (Room.Door door : room.connected.values()) {
			door.set( Room.Door.Type.TUNNEL );
		}
	}

}
