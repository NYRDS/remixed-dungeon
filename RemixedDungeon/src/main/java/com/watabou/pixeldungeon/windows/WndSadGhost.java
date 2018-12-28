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

import com.nyrds.android.util.GuiProperties;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.npcs.Ghost;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.quest.DriedRose;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

import androidx.annotation.NonNull;

public class WndSadGhost extends Window {
	
	private static final int WIDTH		= 120;
	private static final int BTN_HEIGHT	= 18;
	
	public WndSadGhost( final Ghost ghost, final Item item ) {
		
		super();
		
		IconTitle titlebar = new IconTitle();
		titlebar.icon( new ItemSprite( item ) );
		titlebar.label( Utils.capitalize( item.name() ) );
		titlebar.setRect( 0, 0, WIDTH, 0 );
		add( titlebar );
		
		Text message = PixelScene.createMultiline(
				item instanceof DriedRose ? Game.getVar(R.string.WndSadGhost_Rose) : Game.getVar(R.string.WndSadGhost_Rat),
				GuiProperties.regularFontSize()
		);
		message.maxWidth(WIDTH);
		message.y = titlebar.bottom() + GAP;
		add( message );
		
		RedButton btnWeapon = new RedButton( Game.getVar(R.string.WndSadGhost_Wepon) ) {
			@Override
			protected void onClick() {
				selectReward( ghost, item, Ghost.Quest.getWeapon());
			}
		};
		btnWeapon.setRect( 0, message.y + message.height() + GAP, WIDTH, BTN_HEIGHT );
		add( btnWeapon );
		
		RedButton btnArmor = new RedButton( Game.getVar(R.string.WndSadGhost_Armor) ) {
			@Override
			protected void onClick() {
				selectReward( ghost, item, Ghost.Quest.getArmor());
			}
		};
		btnArmor.setRect( 0, btnWeapon.bottom() + GAP, WIDTH, BTN_HEIGHT );
		add( btnArmor );
		
		resize( WIDTH, (int)btnArmor.bottom() );
	}

	private void selectReward(Ghost ghost, @NonNull Item item, @NonNull Item reward ) {
		hide();

		item.removeItemFrom(Dungeon.hero);

		if (reward.doPickUp( Dungeon.hero )) {
			GLog.i( Hero.getHeroYouNowHave(), reward.name() );
		} else {
			Dungeon.level.drop( reward, ghost.getPos() ).sprite.drop();
		}
		
		ghost.say( Game.getVar(R.string.WndSadGhost_Farewell) );
		ghost.die( null );
		
		Ghost.Quest.complete();
	}
}
