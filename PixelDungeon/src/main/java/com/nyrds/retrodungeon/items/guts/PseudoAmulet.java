package com.nyrds.retrodungeon.items.guts;

import com.nyrds.retrodungeon.ml.R;
import com.nyrds.retrodungeon.mobs.guts.MimicAmulet;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

public class PseudoAmulet extends Item {

	public PseudoAmulet() {

		image  = ItemSpriteSheet.AMULET;
		name = Game.getVar(R.string.Amulet_Name);
		info = Game.getVar(R.string.Amulet_Info);
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

		MimicAmulet mimic = new MimicAmulet();
		mimic.setPos(spawnPos);
		mimic.setState(mimic.WANDERING);
		mimic.adjustStats(Dungeon.depth);
		
		Dungeon.level.spawnMob( mimic );
		
		CellEmitter.get( pos ).burst( Speck.factory( Speck.STAR ), 10 );
		Sample.INSTANCE.play( Assets.SND_MIMIC );
		
		return null;
	}
	
}
