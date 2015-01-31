package com.watabou.pixeldungeon.items.bags;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.potions.Potion;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

public class PotionBelt extends Bag {

	{
		name  = Game.getVar(R.string.PotionBelt_Info);
		image = ItemSpriteSheet.BELT;
		
		size = 12;
	}
	
	@Override
	public boolean grab( Item item ) {
		return item instanceof Potion;
	}
	
	@Override
	public int price() {
		return 50;
	}
	
	@Override
	public String info() {
		return Game.getVar(R.string.PotionBelt_Info);
	}
	
}
