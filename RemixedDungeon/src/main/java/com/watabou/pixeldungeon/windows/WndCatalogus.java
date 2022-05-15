/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.windows.ScrollableList;
import com.nyrds.pixeldungeon.windows.WndHelper;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Text;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.potions.Potion;
import com.watabou.pixeldungeon.items.scrolls.Scroll;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.CatalogusListItem;
import com.watabou.pixeldungeon.ui.ScrollPane;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.elements.LabeledTab;
import com.watabou.pixeldungeon.windows.elements.Tab;

public class WndCatalogus extends WndTabbed {

	private static final int ITEM_HEIGHT = 18;

	private Text       txtTitle;
	private ScrollPane list;

	private static boolean showPotions = true;

	public WndCatalogus() {

		super();

		resize(WndHelper.getLimitedWidth(120), WndHelper.getFullscreenHeight() - tabHeight() - MARGIN);

        txtTitle = PixelScene.createText(StringsManager.getVar(R.string.WndCatalogus_Title), GuiProperties.titleFontSize());
		txtTitle.hardlight(Window.TITLE_COLOR);
		add(txtTitle);

		list = new ScrollableList(new Component());

		add(list);
		list.setRect(0, txtTitle.height(), width, height - txtTitle.height());

		boolean showPotions = WndCatalogus.showPotions;
        Tab[] tabs = {
				new LabeledTab(this, StringsManager.getVar(R.string.WndCatalogus_Potions)) {
					public void select(boolean value) {
						super.select(value);
						WndCatalogus.showPotions = value;
						updateList();
					}
				},
				new LabeledTab(this, StringsManager.getVar(R.string.WndCatalogus_Scrolls)) {
					public void select(boolean value) {
						super.select(value);
						WndCatalogus.showPotions = !value;
						updateList();
					}
				}
		};
		for (Tab tab : tabs) {
			tab.setSize(width / tabs.length, tabHeight());
			add(tab);
		}

		select(showPotions ? 0 : 1);
	}

	private void updateList() {

        txtTitle.text(Utils.format(R.string.WndCatalogus_Title, showPotions ?
                StringsManager.getVar(R.string.WndCatalogus_Potions) : StringsManager.getVar(R.string.WndCatalogus_Scrolls)));
		txtTitle.setX(PixelScene.align(PixelScene.uiCamera, (width - txtTitle.width()) / 2));

		Component content = list.content();
		content.clear();
		list.scrollTo(0, 0);

		float pos = 0;
		for (Class<? extends Item> itemClass : showPotions ? Potion.getKnown() : Scroll.getKnown()) {
			CatalogusListItem item = new CatalogusListItem(itemClass);
			item.setRect(0, pos, width, ITEM_HEIGHT);
			content.add(item);

			pos += item.height();
		}

		for (Class<? extends Item> itemClass : showPotions ? Potion.getUnknown() : Scroll.getUnknown()) {
			CatalogusListItem item = new CatalogusListItem(itemClass);
			item.setRect(0, pos, width, ITEM_HEIGHT);
			content.add(item);

			pos += item.height();
		}

		content.setSize(width, pos);
	}

}
