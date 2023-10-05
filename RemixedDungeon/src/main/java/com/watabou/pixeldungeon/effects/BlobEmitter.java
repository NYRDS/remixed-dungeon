
package com.watabou.pixeldungeon.effects;

import com.watabou.noosa.Gizmo;
import com.watabou.noosa.particles.Emitter;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Random;

public class BlobEmitter extends Emitter {
	
	private final Blob blob;
	
	public BlobEmitter( Blob blob ) {
		this.blob = blob;
		blob.use( this );
	}
	
	@Override
	protected void emit( int index ) {

		//GLog.debug("%s %d", blob.getEntityKind(), blob.getVolume());

		if (blob.getVolume() <= 0) {
			return;
		}

		float size = DungeonTilemap.SIZE;
		final Level level = Dungeon.level;

		for (int i = 0; i < level.getLength(); i++) {
			if (blob.cur[i] > 0 && Dungeon.isCellVisible(i)) {
				float x = ((level.cellX(i)) + Random.Float()) * size;
				float y = ((level.cellY(i)) + Random.Float()) * size;



				y+= Gizmo.isometricShift();
				factory.emit( this, index, x, y );
			}
		}
	}
}
