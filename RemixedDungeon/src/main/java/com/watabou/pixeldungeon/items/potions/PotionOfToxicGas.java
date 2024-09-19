
package com.watabou.pixeldungeon.items.potions;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.items.weapon.missiles.Arrow;
import com.watabou.pixeldungeon.items.weapon.missiles.PoisonArrow;
import com.watabou.pixeldungeon.scenes.GameScene;

public class PotionOfToxicGas extends UpgradablePotion {

	{
		labelIndex = 9;
	}
	
	@Override
	public void shatter( int cell ) {
		
		setKnown();
		
		splash( cell );
		Sample.INSTANCE.play( Assets.SND_SHATTER );
		
		ToxicGas gas = Blob.seed( cell, (int) (1000*qualityFactor()), ToxicGas.class );
		GameScene.add( gas );
	}
	
	@Override
	public String desc() {
        return StringsManager.getVar(R.string.PotionOfToxicGas_Info);
    }

	@Override
	public int basePrice() {
		return 40;
	}

	@Override
	protected void moistenArrow(Arrow arrow, Char owner) {
		int quantity = reallyMoistArrows(arrow,owner);
		
		PoisonArrow moistenArrows = new PoisonArrow(quantity);
		owner.collect(moistenArrows);
	}
}
