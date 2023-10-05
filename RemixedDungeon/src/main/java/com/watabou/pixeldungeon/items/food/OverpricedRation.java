
package com.watabou.pixeldungeon.items.food;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.buffs.Hunger;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

public class OverpricedRation extends Food {

	{
		image = ItemSpriteSheet.OVERPRICED;
		energy = Hunger.STARVING - Hunger.HUNGRY;
        message = StringsManager.getVar(R.string.OverpricedRation_Message);
	}
	
	@Override
	public int price() {
		return 20 * quantity();
	}
	
	@Override
	public Item poison(int cell){
		return morphTo(RottenRation.class);
	}
}
