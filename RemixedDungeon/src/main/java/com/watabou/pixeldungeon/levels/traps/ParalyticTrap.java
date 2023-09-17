
package com.watabou.pixeldungeon.levels.traps;

import com.nyrds.pixeldungeon.levels.objects.ITrigger;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.ParalyticGas;
import com.watabou.pixeldungeon.scenes.GameScene;

import org.jetbrains.annotations.Nullable;

public class ParalyticTrap implements ITrigger{

	// 0xCCCC55
	
	public static void trigger( int pos, @Nullable Char ch ) {
		GameScene.add( Blob.seed( pos, 80 + 5 * Dungeon.depth, ParalyticGas.class ) );
	}

	@Override
	public void doTrigger(int cell, Char ch) {
		trigger(cell,ch);
	}
}
