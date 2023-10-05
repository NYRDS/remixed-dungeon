
package com.watabou.pixeldungeon.actors.blobs;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Roots;
import com.watabou.pixeldungeon.effects.BlobEmitter;
import com.watabou.pixeldungeon.effects.particles.LeafParticle;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.scenes.GameScene;

public class Regrowth extends Blob {
	
	@Override
	protected void evolve() {
		super.evolve();

		if (getVolume() > 0) {
			final Level level = Dungeon.level;
			boolean mapUpdated = false;
			
			for (int i=0; i < getLength(); i++) {

				if (off[i] > 0 && level.isPlainTile(i)) {
					int c = level.map[i];

					if (c == Terrain.EMPTY || c == Terrain.EMBERS || c == Terrain.EMPTY_DECO) {
						level.set( i, cur[i] > 9 ? Terrain.HIGH_GRASS : Terrain.GRASS );
						mapUpdated = true;
						
					} else if (c == Terrain.GRASS && cur[i] > 9) {
						
						level.set( i, Terrain.HIGH_GRASS );
						mapUpdated = true;
						
					}
					
					Char ch = Actor.findChar( i );
					if (ch != null) {
						Buff.prolong( ch, Roots.class, TICK );
					}
				}
			}
			
			if (mapUpdated) {
				GameScene.updateMap();
				Dungeon.observe();
			}
		}
	}
	
	@Override
	public void use( BlobEmitter emitter ) {
		super.use( emitter );
		
		emitter.start( LeafParticle.LEVEL_SPECIFIC, 0.2f, 0 );
	}
}
