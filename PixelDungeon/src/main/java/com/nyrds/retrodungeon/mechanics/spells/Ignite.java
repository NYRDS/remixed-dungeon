package com.nyrds.retrodungeon.mechanics.spells;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.Fire;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.scenes.GameScene;

public class Ignite extends Spell{

	Ignite() {
		targetingType = SpellHelper.TARGET_CELL;
		magicAffinity = SpellHelper.AFFINITY_ELEMENTAL;

		level = 2;
		imageIndex = 0;
		spellCost = 2;
	}

	@Override
	public boolean cast(Char chr, int cell){
		if(Dungeon.level.cellValid(cell)) {
			if(Ballistica.cast(chr.getPos(), cell, false, true) == cell) {
				GameScene.add( Blob.seed( cell, 5, Fire.class ) );
				castCallback(chr);
				return true;
			}
		}
		return false;
	}

	@Override
	public String texture(){
		return "spellsIcons/elemental.png";
	}
}
