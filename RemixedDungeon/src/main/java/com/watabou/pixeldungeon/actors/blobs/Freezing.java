
package com.watabou.pixeldungeon.actors.blobs;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Frost;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.particles.SnowParticle;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.utils.Random;

public class Freezing {

	// for newInstance used if Buff.affect
	public Freezing() {
	}
	// It's not really a blob...
	
	public static void affect( int cell ) {


		Blob fire = Dungeon.level.blobs.get( Fire.class );

		Char ch = Actor.findChar( cell ); 
		if (ch != null) {
			Buff.prolong( ch, Frost.class, Frost.duration( ch ) * Random.Float( 1.0f, 1.5f ) );
		}
		
		if (fire != null) {
			fire.clearBlob( cell );
		}
		
		Heap heap = Dungeon.level.getHeap( cell );
		if (heap != null) {
			heap.freeze();
		}

		if (Dungeon.isCellVisible(cell)) {
			CellEmitter.get( cell ).start( SnowParticle.FACTORY, 0.2f, 6 );
		}
	}
}
