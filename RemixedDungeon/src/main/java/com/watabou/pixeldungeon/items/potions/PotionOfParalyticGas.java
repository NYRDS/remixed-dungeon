
package com.watabou.pixeldungeon.items.potions;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.ParalyticGas;
import com.watabou.pixeldungeon.items.weapon.missiles.Arrow;
import com.watabou.pixeldungeon.items.weapon.missiles.ParalysisArrow;
import com.watabou.pixeldungeon.scenes.GameScene;

public class PotionOfParalyticGas extends UpgradablePotion {

	{
		labelIndex = 8;
	}

	@Override
	public void shatter( int cell ) {
		
		setKnown();
		
		splash( cell );
		Sample.INSTANCE.play( Assets.SND_SHATTER );
		
		GameScene.add( Blob.seed( cell, (int) (1000 * qualityFactor()), ParalyticGas.class ) );
	}
	
	@Override
	public String desc() {
        return StringsManager.getVar(R.string.PotionOfParalyticGas_Info);
    }

	@Override
	public int basePrice() {
		return 40;
	}

	@Override
	protected void moistenArrow(Arrow arrow, Char owner) {
		int quantity = reallyMoistArrows(arrow,owner);
		
		ParalysisArrow moistenArrows = new ParalysisArrow(quantity);
		owner.collect(moistenArrows);
	}
}
