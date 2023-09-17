
package com.watabou.pixeldungeon.items.keys;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.DungeonGenerator;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.bags.Bag;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;



public class IronKey extends Key {

	public static int curDepthQuantity = 0;
	
	{
		image = ItemSpriteSheet.IRON_KEY;
	}

	public static void countIronKeys() {
		if(Dungeon.isLoading()) {
			return;
		}

		curDepthQuantity = 0;

		var levelId = DungeonGenerator.getCurrentLevelId();

		for (Item item : Dungeon.hero.getBelongings().backpack) {
			if (item instanceof IronKey && ((IronKey)item).levelId.equals(levelId)) {
				curDepthQuantity++;
			}
		}
	}

	@Override
	public boolean collect(@NotNull Bag bag ) {
		boolean result = super.collect( bag );
		if (result) {
			countIronKeys();
		}
		return result;
	}
	
	@Override
	public void onDetach( ) {
		countIronKeys();
		super.onDetach();
	}
	
	@NotNull
    @Override
	public String toString() {
        return Utils.format(R.string.IronKey_FromDepth, getDepth());
	}
}
