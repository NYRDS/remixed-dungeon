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
package com.nyrds.pixeldungeon.windows;

import com.nyrds.android.util.GuiProperties;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
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
import com.watabou.pixeldungeon.windows.WndTabbed;
import com.watabou.pixeldungeon.windows.elements.LabeledTab;
import com.watabou.pixeldungeon.windows.elements.Tab;

import java.util.ArrayList;

public class WndHeroSpells extends Window {

	private static final int ITEM_HEIGHT = 18;

	private static final String TXT_TITLE   = Game.getVar(R.string.WndSpells_Title);

	private Text       txtTitle;
	private ScrollPane list;

	private ArrayList<CatalogusListItem> items = new ArrayList<>();

	private static boolean showPotions = true;

	public WndHeroSpells() {

		super();

		resize(WndHelper.getLimitedWidth(120), WndHelper.getFullscreenHeight() - MARGIN);

		txtTitle = PixelScene.createText(TXT_TITLE, GuiProperties.titleFontSize());
		txtTitle.hardlight(Window.TITLE_COLOR);
		txtTitle.measure();
		add(txtTitle);

		list = new ScrollableList(new Component());

		add(list);

		list.setRect(0, txtTitle.height(), width, height - txtTitle.height());

		updateList();
	}

	private void updateList() {


		items.clear();

		Component content = list.content();
		content.clear();
		list.scrollTo(0, 0);

		float pos = 0;
		for (Class<? extends Item> itemClass : showPotions ? Potion.getKnown() : Scroll.getKnown()) {
			CatalogusListItem item = new CatalogusListItem(itemClass);
			item.setRect(0, pos, width, ITEM_HEIGHT);
			content.add(item);
			items.add(item);

			pos += item.height();
		}

		content.setSize(width, pos);
	}

}
