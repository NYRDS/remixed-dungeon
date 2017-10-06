package com.nyrds.pixeldungeon.mechanics.spells;

import com.nyrds.pixeldungeon.levels.objects.LevelObject;
import com.nyrds.pixeldungeon.levels.objects.Presser;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.Fire;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Roots;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.effects.Pushing;
import com.watabou.pixeldungeon.effects.particles.EarthParticle;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.scenes.GameScene;

public class WindGust extends Spell{

	WindGust() {
		targetingType = SpellHelper.TARGET_CELL;
		magicAffinity = SpellHelper.AFFINITY_ELEMENTAL;

		level = 3;
		imageIndex = 3;
		spellCost = 2;
	}

	@Override
	public boolean cast(Char chr, int cell) {
		if (Dungeon.level.cellValid(cell)) {
			Char ch;
			boolean triggered = false;

			Ballistica.cast(chr.getPos(), cell, true, false);
			for (int i = 1; i < 3; i++) {
				int c = Ballistica.trace[i];

				if ((ch = Actor.findChar(c)) != null) {
					int next = Ballistica.trace[i + 1];
					if ((Dungeon.level.passable[next] || Dungeon.level.avoid[next])
							&& Actor.findChar(next) == null) {
						ch.move(next);
						ch.getSprite().move(ch.getPos(), next);
						Dungeon.observe();
						triggered = true;
					}
				}

				if (chr instanceof Hero && triggered) {
					Hero hero = (Hero) chr;
					castCallback(hero);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String texture(){
		return "spellsIcons/elemental.png";
	}
}
