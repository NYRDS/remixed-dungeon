
package com.watabou.pixeldungeon.items.scrolls;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.effects.Identification;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndBag;

import org.jetbrains.annotations.NotNull;

public class ScrollOfIdentify extends InventoryScroll {

	{
        inventoryTitle = StringsManager.getVar(R.string.ScrollOfIdentify_InvTitle);
		mode = WndBag.Mode.UNIDENTIFED;
	}

	static public void identify(@NotNull Char ch, @NotNull Item item) {
		GameScene.addToMobLayer( new Identification( ch.getSprite().center().offset( 0, -16 ) ) );

		item.identify();
        GLog.i(Utils.format(R.string.ScrollOfIdentify_Info1, item));

		Badges.validateItemLevelAcquired( item );
	}

	@Override
	protected void onItemSelected(Item item, Char selector) {
		identify(selector,item);
	}

	@Override
	public int price() {
		return isKnown() ? 30 * quantity() : super.price();
	}
}
