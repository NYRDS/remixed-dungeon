/*
 * Pixel Dungeon
 * Copyright (C) 2012-2014  Oleg Dolya
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

import com.nyrds.pixeldungeon.ml.actions.UseItem;
import com.nyrds.pixeldungeon.windows.HBox;
import com.nyrds.pixeldungeon.windows.VHBox;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.ui.ItemSlot;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.Utils;

public class WndItem extends Window {

	private static final float BUTTON_WIDTH		= 36;

	public WndItem( final WndBag bag, final Item item ) {
		
		super();

		int WIDTH = stdWidth();

		IconTitle titlebar = new IconTitle(new ItemSprite( item ), Utils.capitalize( item.toString() ));
		titlebar.setRect( 0, 0, WIDTH, 0 );
		add( titlebar );
		
		if (item.isLevelKnown() && item.level() > 0) {
			titlebar.color( ItemSlot.UPGRADED );
		} else if (item.isLevelKnown() && item.level() < 0) {
			titlebar.color( ItemSlot.DEGRADED );
		}

		Text info = PixelScene.createMultiline( item.info(), GuiProperties.regularFontSize());
		info.maxWidth(WIDTH);
		info.setX(titlebar.left());
		info.setY(titlebar.bottom() + GAP);
		add(info);
		 
		float y = info.getY() + info.height() + GAP;

		VHBox actions = new VHBox(WIDTH);
		actions.setAlign(HBox.Align.Width);
		actions.setGap(GAP);

		Char owner = item.getOwner();

		if (bag != null && item.getOwner().isAlive()) {
			for (final String action:item.actions( owner )) {

				if(owner.getHeroClass().forbidden(action)){
					continue;
				}

				RedButton btn = new RedButton(StringsManager.maybeId(action) ) {
					@Override
					protected void onClick() {
						hide();
						bag.hide();
						owner.nextAction(new UseItem(item, action));
					}
				};
				btn.setSize( Math.max( BUTTON_WIDTH, btn.reqWidth() ), BUTTON_HEIGHT );

				actions.add(btn);
			}
		}

		add(actions);
		actions.setPos(titlebar.left(), y);

		resize( WIDTH, (int) (actions.bottom() + GAP));
	}
}
