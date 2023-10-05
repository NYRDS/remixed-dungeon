
package com.watabou.pixeldungeon.actors.blobs;

import com.nyrds.LuaInterface;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Journal;
import com.watabou.pixeldungeon.effects.BlobEmitter;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.levels.Level;

public class Alchemy extends Blob {

	@Override
	protected void evolve() {
		for (int i = 0;i<getLength();i++) {
			off[i] = cur[i];
			if(cur[i]>0) {
				setVolume(getVolume() + cur[i]);
				if (Dungeon.isCellVisible(i)) {
					Journal.add(Journal.Feature.ALCHEMY.desc());
				}
			}
		}
	}

	@LuaInterface
	public static void transmute( int cell ) {
		final Level level = Dungeon.level;

		Heap heap = level.getHeap( cell );
		if (heap != null) {
			
			Item result = heap.transmute();
			if (result != null) {
				level.animatedDrop( result, cell );
			}
		}
	}
	
	@Override
	public void use( BlobEmitter emitter ) {
		super.use( emitter );	
		emitter.start( Speck.factory( Speck.BUBBLE ), 0.4f, 0 );
	}
}
