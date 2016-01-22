package com.watabou.pixeldungeon.items.food;

import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Hunger;
import com.watabou.pixeldungeon.actors.mobs.MimicPie;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

public class PseudoPasty extends Food {

	public PseudoPasty() {
		image  = ItemSpriteSheet.PASTY;
		energy = Hunger.STARVING;
	}
	
	@Override
	public Item pick(Char ch, int pos ) {
		int spawnPos = pos;
		
		if(ch.getPos() == pos) {
			spawnPos = Dungeon.level.getEmptyCellNextTo(ch.getPos());
			
			if (!Dungeon.level.cellValid(spawnPos)) {
				return this;
			}
		}
		
		MimicPie mob = new MimicPie();
		mob.setPos(spawnPos);
		mob.state = mob.WANDERING;
		mob.adjustStats(Dungeon.depth);
		
		Dungeon.level.spawnMob( mob );
		
		CellEmitter.get( pos ).burst( Speck.factory( Speck.STAR ), 10 );
		Sample.INSTANCE.play( Assets.SND_MIMIC );
		
		return null;
	}
	
}
