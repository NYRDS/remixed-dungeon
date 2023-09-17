
package com.watabou.pixeldungeon.levels.features;

import com.nyrds.pixeldungeon.items.Treasury;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Barkskin;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Invisibility;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.particles.LeafParticle;
import com.watabou.pixeldungeon.items.Dewdrop;
import com.watabou.pixeldungeon.items.rings.RingOfHerbalism.Herbalism;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.utils.Random;

import org.jetbrains.annotations.Nullable;

public class HighGrass {

	public static void trample( Level level, int pos, @Nullable Char ch ) {
		
		level.set( pos, Terrain.GRASS );
		GameScene.updateMap( pos );

		if (ch != null) {
			int herbalismLevel = ch.buffLevel(Herbalism.class);

			// Dew
			if (herbalismLevel >= 0 && Random.Int(6) <= Random.Int(herbalismLevel + 1)) {
				Treasury.getLevelTreasury().random(Dewdrop.class.getSimpleName()).doDrop(ch);
			}

			// Seed
			if (herbalismLevel >= 0 && Random.Int(18) <= Random.Int(herbalismLevel + 1)) {
				Treasury.getLevelTreasury().random(Treasury.Category.SEED).doDrop(ch);
			}
		}

		int leaves = 4;

		if(ch != null) {
			// Barkskin
			if (ch.getSubClass() == HeroSubClass.WARDEN) {
				Buff.affect(ch, Barkskin.class).level(ch.ht() / 3);
				leaves = 8;
			}

			if (ch.getSubClass() == HeroSubClass.SCOUT) {
				Buff.prolong(ch, Invisibility.class, 5);
				leaves = 2;
			}
		}
		
		CellEmitter.get(pos).burst(LeafParticle.LEVEL_SPECIFIC, leaves);
		Dungeon.observe();
	}
}
