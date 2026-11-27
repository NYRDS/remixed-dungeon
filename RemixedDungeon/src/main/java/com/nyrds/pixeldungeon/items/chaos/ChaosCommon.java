package com.nyrds.pixeldungeon.items.chaos;

import com.nyrds.LuaInterface;
import com.nyrds.platform.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.particles.PurpleParticle;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.utils.Random;

public class ChaosCommon {
	
	private static final String[] blobs = {
		"ConfusionGas",
		"LiquidFlame",
		"ParalyticGas",
		"Regrowth",
		"ToxicGas",
	};
	
	@SuppressWarnings("unchecked")
	@LuaInterface
	public static void doChaosMark(int cell, int charge) {
		if(charge > 0) {
			CellEmitter.center( cell ).burst( PurpleParticle.BURST, Random.IntRange( 10, 20 ) );
			Sample.INSTANCE.play( Assets.SND_CRYSTAL );
			GameScene.add(Blob.seed(cell, charge, Random.element(blobs)));
			GameScene.add(Blob.seed(cell, charge, Random.element(blobs)));
		}
	}
}
