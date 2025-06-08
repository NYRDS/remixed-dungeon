
package com.watabou.pixeldungeon.items.scrolls;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.windows.WndBag;
import com.watabou.pixeldungeon.windows.WndOptions;

import org.jetbrains.annotations.NotNull;

public abstract class InventoryScroll extends Scroll {

	protected String inventoryTitle = StringsManager.getVar(R.string.InventoryScroll_Title);
    protected WndBag.Mode mode = WndBag.Mode.ALL;

	private Char reader;

	@Override
	protected void doRead(@NotNull Char reader) {
		
		if (!isKnown()) {
			setKnown();
			identifiedByUse = true;
		} else {
			identifiedByUse = false;
		}

		this.reader = reader;

		GameScene.selectItem(reader, new ScrollUse(this).invoke(), mode, inventoryTitle);
	}
	
	void confirmCancellation() {
        GameScene.show( new WndOptions( name(),
                StringsManager.getVar(R.string.InventoryScroll_Warning),
                StringsManager.getVar(R.string.InventoryScroll_Yes),
                StringsManager.getVar(R.string.InventoryScroll_No)) {
			@Override
			public void onSelect(int index) {
				switch (index) {
				case 0:
					getOwner().spend( TIME_TO_READ );
					identifiedByUse = false;
					break;
				case 1:
					GameScene.selectItem(reader, new ScrollUse(InventoryScroll.this).invoke(), mode, inventoryTitle);
					break;
				}
			}
			public void onBackPressed() {}
		} );
	}

	protected abstract void onItemSelected(Item item, Char selector);

	protected static boolean identifiedByUse = false;

}
