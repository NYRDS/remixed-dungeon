/*
 * Pixel Dungeon
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.watabou.pixeldungeon.effects;

import com.watabou.noosa.Gizmo;
import com.watabou.noosa.particles.Emitter;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.utils.Random;

public class BlobEmitter extends Emitter {
	
	private final Blob blob;
	
	public BlobEmitter( Blob blob ) {
		this.blob = blob;
		blob.use( this );
	}
	
	@Override
	protected void emit( int index ) {
		
		if (blob.volume <= 0) {
			return;
		}
		
		int[] map = blob.cur;
		float size = DungeonTilemap.SIZE;

		final Level level = Dungeon.level;

		for (int i = 0; i < level.getLength(); i++) {
			if (map[i] > 0 && Dungeon.isCellVisible(i)) {
				float x = ((i % level.getWidth()) + Random.Float()) * size;
				float y = ((i / level.getWidth()) + Random.Float()) * size;


				y+= Gizmo.isometricShift();

				factory.emit( this, index, x, y );
			}
		}
	}
}
