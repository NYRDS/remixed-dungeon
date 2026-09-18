
package com.watabou.pixeldungeon.actors.blobs;
import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.mechanics.buffs.BuffFactory;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.particles.SnowParticle;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.utils.Random;

@LuaInterface
public class Freezing {

	// for newInstance used if Buff.affect
	public Freezing() {
	}
	// It's not really a blob...
	
	public static void affect( int cell ) {
		Level level = Dungeon.level;
		Blob fire = level.blobs.get( Fire.class );
		Blob liquidFlame = level.blobs.get( LiquidFlame.class );

		Char ch = Actor.findChar( cell ); 
		if (ch != null) {
			Buff.prolong( ch, BuffFactory.FROST, CharUtils.durationFactor(ch) * 5f * Random.Float( 1.0f, 1.5f ) );
		}
		
		if (fire != null) {
			fire.clearBlob( cell );
		}

		if (liquidFlame != null) {
			liquidFlame.clearBlob( cell );
		}
		
		Heap heap = level.getHeap( cell );
		if (heap != null) {
			heap.freeze();
		}

		if (Dungeon.isCellVisible(cell)) {
			CellEmitter.get( cell ).start( SnowParticle.FACTORY, 0.2f, 6 );
		}
	}
}
