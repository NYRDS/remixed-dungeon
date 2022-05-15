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

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.npcs.Imp;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.quest.DwarfToken;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

public class WndImp extends Window {

	private static final int WIDTH		= 120;
	private static final int BTN_HEIGHT	= 18;

	public WndImp( final Imp imp, final DwarfToken tokens ) {
		
		super();
		
		IconTitle titlebar = new IconTitle();
		titlebar.icon( new ItemSprite( tokens ) );
		titlebar.label( Utils.capitalize( tokens.name() ) );
		titlebar.setRect( 0, 0, WIDTH, 0 );
		add( titlebar );

        Text message = PixelScene.createMultiline(R.string.WndImp_Message, GuiProperties.regularFontSize() );
		message.maxWidth(WIDTH);
		message.setY(titlebar.bottom() + GAP);
		add( message );

        RedButton btnReward = new RedButton(R.string.WndImp_Reward) {
			@Override
			protected void onClick() {
				takeReward( imp, tokens, Imp.Quest.reward );
			}
		};
		btnReward.setRect( 0, message.getY() + message.height() + GAP, WIDTH, BTN_HEIGHT );
		add( btnReward );
		
		resize( WIDTH, (int)btnReward.bottom() );
	}
	
	private void takeReward( Imp imp, DwarfToken tokens, Item reward ) {
		
		hide();
		
		tokens.detachAll( Dungeon.hero.getBelongings().backpack );

		reward.identify();
		if (reward.doPickUp( Dungeon.hero )) {
			GLog.i( Hero.getHeroYouNowHave(), reward.name() );
		} else {
			reward.doDrop(imp);
		}
		
		imp.flee();
		
		Imp.Quest.complete();
	}
}
