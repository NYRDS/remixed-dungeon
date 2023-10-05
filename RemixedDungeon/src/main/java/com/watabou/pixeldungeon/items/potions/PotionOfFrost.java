
package com.watabou.pixeldungeon.items.potions;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Freezing;
import com.watabou.pixeldungeon.items.weapon.missiles.Arrow;
import com.watabou.pixeldungeon.items.weapon.missiles.FrostArrow;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.utils.BArray;
import com.watabou.utils.PathFinder;

public class PotionOfFrost extends UpgradablePotion {

	{
		labelIndex = 0;
	}

	private static final int DISTANCE	= 2;
	
	@Override
	public void shatter( int cell ) {
		
		if( !canShatter() ) {
			return;
		}

		final Level level = Dungeon.level;

		PathFinder.buildDistanceMap( cell, BArray.not( level.losBlocking, null ), (int) (DISTANCE * qualityFactor()));

		for (int i = 0; i < level.getLength(); i++) {
			if (PathFinder.distance[i] < Integer.MAX_VALUE) {
				Freezing.affect( i );
			}
		}
		
		splash( cell );
		Sample.INSTANCE.play( Assets.SND_SHATTER );
		
		setKnown();
	}
	
	@Override
	public String desc() {
        return StringsManager.getVar(R.string.PotionOfFrost_Info);
    }

	@Override
	public int basePrice() {
		return 50;
	}
	
	@Override
	protected void moistenArrow(Arrow arrow, Char owner) {
		int quantity = reallyMoistArrows(arrow, owner);
		
		FrostArrow moistenArrows = new FrostArrow(quantity);
		owner.collect(moistenArrows);
	}
}
