package com.nyrds.pixeldungeon.mechanics.spells;

import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.Fire;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Frost;
import com.watabou.pixeldungeon.actors.buffs.Slow;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.effects.particles.SnowParticle;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.utils.Random;

public class FreezeGlobe extends Spell{

	FreezeGlobe() {
		targetingType = SpellHelper.TARGET_CELL;
		magicAffinity = SpellHelper.AFFINITY_ELEMENTAL;

		level = 2;
		imageIndex = 1;
		spellCost = 5;
	}

	@Override
	public boolean cast(Char chr, int cell){
		if(!Dungeon.level.cellValid(cell)) {
			return false;
		}
		boolean triggered = false;
		if(Ballistica.cast(chr.getPos(), cell, false, true) == cell) {

			Level level = Dungeon.level;
			int x = level.cellX(cell);
			int y = level.cellY(cell);

			for (int i = 0; i < 3; i++){
				for (int j = 0; j < 3; j++){
					int currentCell = level.cell(x - 1 + i, y - 1 + j);
					if(Dungeon.level.cellValid(currentCell)) {
						Char ch = Actor.findChar( currentCell );
						if (ch != null) {
							ch.getSprite().emitter().burst( SnowParticle.FACTORY, 5 );
							ch.getSprite().burst( 0xFF99FFFF, 3 );

							Buff.affect( ch, Frost.class, Frost.duration( ch ) );
							Buff.affect( ch, Slow.class, Slow.duration( ch ) );
							Sample.INSTANCE.play( Assets.SND_SHATTER );
							triggered = true;
						}
					}
				}
			}

			if(chr instanceof Hero && triggered) {
				Hero hero = (Hero) chr;
				castCallback(hero);
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
