
package com.watabou.pixeldungeon.items.potions;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.LiquidFlame;
import com.watabou.pixeldungeon.items.weapon.missiles.Arrow;
import com.watabou.pixeldungeon.items.weapon.missiles.FireArrow;
import com.watabou.pixeldungeon.scenes.GameScene;

public class PotionOfLiquidFlame extends UpgradablePotion {

	{
		labelIndex = 1;
	}

	@Override
	public void shatter( int cell ) {
		
		setKnown();
		
		splash( cell );
		Sample.INSTANCE.play( Assets.SND_SHATTER );
		
		LiquidFlame fire = Blob.seed( cell, (int) (10 * qualityFactor()), LiquidFlame.class );
		GameScene.add( fire );
	}
	
	@Override
	public String desc() {
        return StringsManager.getVar(R.string.PotionOfLiquidFlame_Info);
    }

	@Override
	public int basePrice() {
		return 40;
	}

	@Override
	protected void moistenArrow(Arrow arrow, Char owner) {
		int quantity = reallyMoistArrows(arrow,owner);
		
		FireArrow moistenArrows = new FireArrow(quantity);
		owner.collect(moistenArrows);
	}
}
