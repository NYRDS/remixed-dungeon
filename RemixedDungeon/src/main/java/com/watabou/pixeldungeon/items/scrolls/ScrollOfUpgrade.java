
package com.watabou.pixeldungeon.items.scrolls;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.windows.WndBag;

public class ScrollOfUpgrade extends InventoryScroll {

	{
        inventoryTitle = StringsManager.getVar(R.string.ScrollOfUpgrade_InvTitle);
		mode = WndBag.Mode.UPGRADEABLE;
	}
	
	@Override
	protected void onItemSelected(Item item, Char selector) {

		ScrollOfRemoveCurse.uncurse( selector, item );
		item.upgrade();

        GLog.p(StringsManager.getVar(R.string.ScrollOfUpgrade_LooksBetter), item.name() );
		
		Badges.validateItemLevelAcquired( item );
		
		upgrade( selector );
	}
	
	public static void upgrade(Char hero ) {
		hero.getSprite().emitter().start( Speck.factory( Speck.UP ), 0.2f, 3 );
	}
}
