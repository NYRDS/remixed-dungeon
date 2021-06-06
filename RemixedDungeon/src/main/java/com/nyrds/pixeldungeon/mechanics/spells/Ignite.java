package com.nyrds.pixeldungeon.mechanics.spells;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.Fire;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.particles.FlameParticle;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.scenes.GameScene;

import org.jetbrains.annotations.NotNull;

public class Ignite extends Spell{

	Ignite() {
		targetingType = SpellHelper.TARGET_CELL;
		magicAffinity = SpellHelper.AFFINITY_ELEMENTAL;

		level = 2;
		image = 1;
		spellCost = 2;
	}

	@Override
	public boolean cast(@NotNull Char chr, int cell){
		if(chr.level().cellValid(cell)) {
			int target = Ballistica.cast(chr.getPos(), cell, true, true);

			CellEmitter.center(target).burst( FlameParticle.FACTORY, chr.skillLevel() );

			GameScene.add( Blob.seed( target, 5, Fire.class ) );
			castCallback(chr);
			return true;
		}
		return false;
	}

	@Override
	public String texture(){
		return "spellsIcons/elemental.png";
	}
}
