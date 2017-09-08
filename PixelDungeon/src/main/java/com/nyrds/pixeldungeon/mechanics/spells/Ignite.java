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

/**
 * Created by mike on 05.09.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

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
		if(Dungeon.level.cellValid(cell)) {
			for (int i=1; i < Ballistica.distance - 1; i++) {
				int c = Ballistica.trace[i];
				if (Dungeon.level.flammable[c]) {
					GameScene.add( Blob.seed( c, 1, Fire.class ) );
				}
			}

			GameScene.add( Blob.seed( cell, 1, Fire.class ) );

			Char ch = Actor.findChar( cell );
			if (ch != null) {

				ch.damage( Random.Int( 1, 8 ), this );
				Buff.affect( ch, Burning.class ).reignite( ch );

				ch.getSprite().emitter().burst( FlameParticle.FACTORY, 5 );
			}
			return true;
		}
		return false;
	}

	@Override
	public String texture(){
		return "spellsIcons/elemental.png";
	}
}
