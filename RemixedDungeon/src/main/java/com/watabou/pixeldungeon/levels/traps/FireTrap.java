
package com.watabou.pixeldungeon.levels.traps;

import com.nyrds.pixeldungeon.levels.objects.ITrigger;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.Fire;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.particles.FlameParticle;
import com.watabou.pixeldungeon.scenes.GameScene;

import org.jetbrains.annotations.Nullable;

public class FireTrap implements ITrigger {

	// 0xFF7708
	
	public static void trigger( int pos, @Nullable Char ch ) {
		GameScene.add( Blob.seed( pos, 2, Fire.class ) );
		CellEmitter.get( pos ).burst( FlameParticle.FACTORY, 5 );
	}

	@Override
	public void doTrigger(int cell, Char ch) {
		trigger(cell,ch);
	}
}
