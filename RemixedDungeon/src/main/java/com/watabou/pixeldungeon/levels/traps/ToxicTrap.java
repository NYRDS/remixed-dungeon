
package com.watabou.pixeldungeon.levels.traps;

import com.nyrds.pixeldungeon.levels.objects.ITrigger;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.scenes.GameScene;

import org.jetbrains.annotations.Nullable;

public class ToxicTrap implements ITrigger{

	// 0x40CC55
	
	public static void trigger( int pos, @Nullable Char ch ) {
		GameScene.add( Blob.seed( pos, 300 + 20 * Dungeon.depth, ToxicGas.class ) );
	}

	@Override
	public void doTrigger(int cell, Char ch) {
		trigger(cell,ch);
	}
}
