package com.nyrds.pixeldungeon.mechanics.spells;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.ResultDescriptions;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.Fire;
import com.watabou.pixeldungeon.actors.blobs.LiquidFlame;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.effects.particles.FlameParticle;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Random;

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
		if(Ballistica.cast(chr.getPos(), cell, false, true) == cell) {
			GameScene.add( Blob.seed( cell, 5, Fire.class ) );
			return true;
		}
		return false;
	}

	@Override
	public String texture(){
		return "spellsIcons/elemental.png";
	}
}
