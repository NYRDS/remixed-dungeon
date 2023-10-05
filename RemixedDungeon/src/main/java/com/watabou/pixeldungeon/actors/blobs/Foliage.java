
package com.watabou.pixeldungeon.actors.blobs;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Journal;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Shadows;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.effects.BlobEmitter;
import com.watabou.pixeldungeon.effects.particles.ShaftParticle;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.scenes.GameScene;

public class Foliage extends Blob {
	
	@Override
	protected void evolve() {

		int from = getWidth() + 1;
		int to   = getLength() - getWidth() - 1;
		
		int[] map = Dungeon.level.map;
		boolean regrowth = false;
		
		boolean visible = false;
		
		for (int pos=from; pos < to; pos++) {
			if (cur[pos] > 0) {
				
				off[pos] = cur[pos];
				setVolume(getVolume() + off[pos]);
				
				if (map[pos] == Terrain.EMBERS || map[pos] == Terrain.EMPTY) {
					map[pos] = Terrain.GRASS;
					regrowth = true;
				}
				
				visible = visible || Dungeon.isCellVisible(pos);
				
			} else {
				off[pos] = 0;
			}
		}
		
		Hero hero = Dungeon.hero;
		if (hero.isAlive() && hero.visibleEnemies() == 0 && cur[hero.getPos()] > 0) {
			Buff.affect( hero, Shadows.class ).prolong();
		}
		
		if (regrowth) {
			GameScene.updateMap();
		}
		
		if (visible) {
			Journal.add( Journal.Feature.GARDEN.desc() );
		}
	}
	
	@Override
	public void use( BlobEmitter emitter ) {
		super.use( emitter );	
		emitter.start( ShaftParticle.FACTORY, 0.9f, 0 );
	}
	
	@Override
	public String tileDesc() {
        return StringsManager.getVar(R.string.Foliage_Info);
    }
}
