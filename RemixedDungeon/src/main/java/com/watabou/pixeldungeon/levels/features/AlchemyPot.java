
package com.watabou.pixeldungeon.levels.features;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.windows.WndBag;

public class AlchemyPot {
	
	private static Char hero;
	private static int pos;

	@LuaInterface
	public static void operate(Char hero, int pos ) {
		
		AlchemyPot.hero = hero;
		AlchemyPot.pos = pos;

        GameScene.selectItem(hero, itemSelector, WndBag.Mode.SEED, StringsManager.getVar(R.string.AlchemyPot_SelectSeed));
	}
	
	private static final WndBag.Listener itemSelector = new WndBag.Listener() {
		@Override
		public void onSelect(Item item, Char selector) {
			if (item != null) {
				item.cast( hero, pos );
			}
		}
	};
}
