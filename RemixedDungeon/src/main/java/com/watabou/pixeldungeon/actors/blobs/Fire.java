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
package com.watabou.pixeldungeon.actors.blobs;

import com.nyrds.pixeldungeon.levels.objects.LevelObject;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.effects.BlobEmitter;
import com.watabou.pixeldungeon.effects.particles.FlameParticle;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.scenes.GameScene;

public class Fire extends Blob {
	
	@Override
	protected void evolve() {

		final Level level = Dungeon.level;
		boolean[] flammable = level.flammable;
		
		int from = getWidth() + 1;
		int to   = getLength() - getWidth() - 1;
		
		boolean observe = false;
		
		for (int pos=from; pos < to; pos++) {
			
			int fire;
			
			if (cur[pos] > 0) {
				
				burn( pos );
				
				fire = cur[pos] - 1;
				if (fire <= 0 && flammable[pos]) {
					
					level.set( pos, Terrain.EMBERS );
					
					observe = true;
					GameScene.updateMapPair(pos);

					if (Dungeon.isCellVisible(pos)) {
						GameScene.discoverTile( pos);
					}
				}
				
			} else {
				
				if (flammable[pos] && (cur[pos-1] > 0 || cur[pos+1] > 0 || cur[pos-getWidth()] > 0 || cur[pos+getWidth()] > 0)) {
					fire = 4;
					burn( pos );
				} else {
					fire = 0;
				}

			}
			
			setVolume(getVolume() + (off[pos] = fire));

		}
		
		if (observe) {
			Dungeon.observe();
		}
	}
	
	public static void burn( int pos ) {
		//GLog.debug("Burn %d", pos);
		Char ch = Actor.findChar( pos );
		if (ch != null) {
			Buff.affect( ch, Burning.class ).reignite( ch );
		}

		final Level level = Dungeon.level;

		Heap heap = level.getHeap( pos );
		if (heap != null) {
			heap.burn();
		}

		LevelObject levelObject = level.getTopLevelObject(pos);
		if (levelObject != null) {
			//GLog.debug("Obj %s %d", levelObject.getEntityKind(), pos);
			levelObject.burn();
		}
	}
	
	@Override
	public void use( BlobEmitter emitter ) {
		super.use( emitter );
		emitter.start( FlameParticle.FACTORY, 0.03f, 0 );
	}
	
	@Override
	public String tileDesc() {
        return StringsManager.getVar(R.string.Fire_Info);
    }
}
