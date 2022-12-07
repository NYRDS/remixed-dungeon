package com.nyrds.retrodungeon.mechanics.spells;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.LiquidFlame;
import com.watabou.pixeldungeon.scenes.GameScene;

/**
 * Created by mike on 05.09.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class Fireball  extends Spell{

	Fireball() {
		targetingType = SpellHelper.TARGET_CELL;
		magicAffinity = SpellHelper.AFFINITY_NECROMANCY;

		name          = "Testing fireball";
		desc          = "Testing fireball";
		imageIndex = 0;

		spellCost = 0;
	}

	@Override
	public boolean cast(Char chr, int cell){

		if(Dungeon.level.cellValid(cell)) {
			LiquidFlame fire = Blob.seed( cell, 50, LiquidFlame.class );
			GameScene.add( fire );
			return true;
		}
		return false;
	}
}
