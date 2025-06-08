
package com.watabou.pixeldungeon.items.scrolls;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.weapon.Weapon;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.windows.WndBag;

public class ScrollOfWeaponUpgrade extends InventoryScroll {

	{
        inventoryTitle = StringsManager.getVar(R.string.ScrollOfWeaponUpgrade_InvTitle);
		mode = WndBag.Mode.UPGRADABLE_WEAPON;
	}
	
	@Override
	protected void onItemSelected(Item item, Char selector) {
		
		Weapon weapon = (Weapon)item;
		
		ScrollOfRemoveCurse.uncurse( selector, weapon );
		weapon.upgrade( true );

        GLog.p(StringsManager.getVar(R.string.ScrollOfWeaponUpgrade_LooksBetter), weapon.name() );
		
		Badges.validateItemLevelAcquired( weapon );
		
		selector.getSprite().emitter().start( Speck.factory( Speck.UP ), 0.2f, 3 );
	}
}
