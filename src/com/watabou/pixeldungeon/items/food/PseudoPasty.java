package com.watabou.pixeldungeon.items.food;

import java.util.ArrayList;

import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Hunger;
import com.watabou.pixeldungeon.actors.mobs.MimicPie;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

public class PseudoPasty extends Food {

	public PseudoPasty() {
		image  = ItemSpriteSheet.PASTY;
		energy = Hunger.STARVING;
	}
	
	@Override
	public Item pick(Char ch, int pos ) {
		int spawnPos = pos;
		
		if(ch.pos == pos) {
			ArrayList<Integer> candidates = new ArrayList<Integer>();
			for (int n : Level.NEIGHBOURS8) {
				int cell = pos + n;
				if ((Level.passable[cell] || Level.avoid[cell]) && Actor.findChar( cell ) == null) {
					candidates.add( cell );
				}
			}
			
			if (candidates.size() > 0) {
				spawnPos = Random.element( candidates );
			} else {
				return this;
			}
		}
		
		MimicPie mob = new MimicPie();
		mob.pos = spawnPos;
		mob.adjustStats(Dungeon.depth);
		
		Dungeon.level.spawnMob( mob );
		
		CellEmitter.get( pos ).burst( Speck.factory( Speck.STAR ), 10 );
		Sample.INSTANCE.play( Assets.SND_MIMIC );
		
		return null;
	}
	
}
