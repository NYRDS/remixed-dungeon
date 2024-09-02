
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

		for (int pos=from; pos < to; pos++) {
			
			int fire;
			
			if (cur[pos] > 0) {
				burn( pos );
				fire = cur[pos] - 1;
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

		if(pos == level.entrance || level.isExit(pos)) {
			return;
		}

		if(level.flammable[pos]) {
			level.set( pos, Terrain.EMBERS );
			GameScene.updateMapPair(pos);

			if (Dungeon.isCellVisible(pos)) {
				GameScene.discoverTile( pos);
			}
			Dungeon.observe();
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
