package com.nyrds.pixeldungeon.mechanics.spells;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.LiquidFlame;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.scenes.GameScene;

public class Ignite extends Spell{

	Ignite() {
		targetingType = SpellHelper.TARGET_CELL;
		magicAffinity = SpellHelper.AFFINITY_ELEMENTAL;

		name          = "Ignite";
		desc          = "Testing fireball";
		imageIndex = 0;
		spellCost = 1;
	}

	@Override
	public boolean cast(Char chr, int cell){
		if(Ballistica.cast(chr.getPos(), cell, false, true) == cell){
			LiquidFlame fire = Blob.seed( cell, 1, LiquidFlame.class );
			GameScene.add( fire );
			return true;
		}
		return false;
	}

	@Override
	public String texture(){
		return "spellsIcons/elemental.png";
	}
}
