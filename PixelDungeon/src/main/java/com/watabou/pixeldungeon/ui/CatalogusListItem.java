package com.watabou.pixeldungeon.ui;

import com.nyrds.retrodungeon.ml.EventCollector;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.windows.WndInfoItem;

/**
 * Created by mike on 15.05.2017.
 * This file is part of Remixed Pixel Dungeon.
 */
public class CatalogusListItem extends ListItem {

	private Item item;

	public CatalogusListItem(Class<? extends Item> cl) {
		super();

		try {
			item = cl.newInstance();
			if (clickable = item.isIdentified()) {
				sprite.copy(new ItemSprite(item));
				label.text(item.name());
			} else {
				sprite.copy(new ItemSprite());
				label.text(item.trueName());
				label.hardlight(0xCCCCCC);
			}
		} catch (Exception e) {
			EventCollector.logException(e);
		}
		add(sprite);
	}

	@Override
	public void onClick() {
		GameScene.show(new WndInfoItem(item));
	}
}
