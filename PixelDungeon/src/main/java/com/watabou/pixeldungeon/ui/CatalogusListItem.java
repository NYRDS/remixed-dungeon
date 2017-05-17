package com.watabou.pixeldungeon.ui;

import com.nyrds.android.util.GuiProperties;
import com.watabou.noosa.Text;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.windows.WndInfoItem;

/**
 * Created by mike on 15.05.2017.
 * This file is part of Remixed Pixel Dungeon.
 */
public class CatalogusListItem extends Component {

	private Item    item;
	private boolean identified;

	private ItemSprite sprite;
	private Text       label;

	public CatalogusListItem(Class<? extends Item> cl) {
		super();

		try {
			item = cl.newInstance();
			if (identified = item.isIdentified()) {
				sprite.view(item);
				label.text(item.name());
			} else {
				sprite.view(Assets.ITEMS, 127, null);
				label.text(item.trueName());
				label.hardlight(0xCCCCCC);
			}
		} catch (Exception e) {
			// Do nothing
		}
	}

	@Override
	protected void createChildren() {
		sprite = new ItemSprite();
		add(sprite);

		label = PixelScene.createText(GuiProperties.regularFontSize());
		add(label);
	}

	@Override
	protected void layout() {
		sprite.y = PixelScene.align(y + (height - sprite.height) / 2);

		label.x = sprite.x + sprite.width;
		label.y = PixelScene.align(y + (height - label.baseLine()) / 2);
	}

	public boolean onClick(float x, float y) {
		if (identified && inside(x, y)) {
			GameScene.show(new WndInfoItem(item));
			return true;
		} else {
			return false;
		}
	}
}
