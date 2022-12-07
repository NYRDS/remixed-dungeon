package com.nyrds.retrodungeon.items.chaos;

import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.ConfusionGas;
import com.watabou.pixeldungeon.actors.blobs.LiquidFlame;
import com.watabou.pixeldungeon.actors.blobs.ParalyticGas;
import com.watabou.pixeldungeon.actors.blobs.Regrowth;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.particles.PurpleParticle;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.utils.Random;

public class ChaosCommon {
	public static final String CHARGE_KEY = "charge";
	
	@SuppressWarnings("rawtypes")
	private static Class[] blobs = {
		ConfusionGas.class,
		LiquidFlame.class,
		ParalyticGas.class,
		Regrowth.class,
		ToxicGas.class,
	};
	
	@SuppressWarnings("unchecked")
	public static void doChaosMark(int cell, int charge) {
		if(charge > 0) {
			CellEmitter.center( cell ).burst( PurpleParticle.BURST, Random.IntRange( 10, 20 ) );
			Sample.INSTANCE.play( Assets.SND_CRYSTAL );
			GameScene.add(Blob.seed(cell, charge, Random.element(blobs)));
			GameScene.add(Blob.seed(cell, charge, Random.element(blobs)));
		}
	}
}
