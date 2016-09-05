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
import com.nyrds.pixeldungeon.items.common.MasteryItem;
import com.nyrds.pixeldungeon.items.necropolis.BlackSkullOfMastery;
import com.watabou.noosa.Game;
import com.watabou.noosa.Text;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.TomeOfMastery;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.Utils;

public class WndChooseWay extends Window {
	
	private static final String TXT_MESSAGE	= Game.getVar(R.string.WndChooseWay_Message);
	private static final String TXT_CANCEL	= Game.getVar(R.string.WndChooseWay_Cancel);
	
	private static final int WIDTH		= 120;
	private static final int BTN_HEIGHT	= 18;
	private static final float GAP		= 2;

	public WndChooseWay(final MasteryItem item, final HeroSubClass way){
		super();
		chooseWay( item, way, null );
	}

	public WndChooseWay( final MasteryItem item, final HeroSubClass way1, final HeroSubClass way2 ) {
		super();
		chooseWay( item, way1, way2 );
	}

	public String getWayDesc( final HeroSubClass way1, final HeroSubClass way2 ){

		String desc =  way1.desc();
		if (way2 != null){
			desc = desc + "\n\n" + way2.desc();
		}
		desc = desc + "\n\n" + TXT_MESSAGE;

		return desc;
	}

	public void chooseWay( final MasteryItem item, final HeroSubClass way1, final HeroSubClass way2 ) {
		float bottom = .0f;

		IconTitle titlebar = new IconTitle();
		titlebar.icon( new ItemSprite( item ) );
		titlebar.label( item.name() );
		titlebar.setRect( 0, 0, WIDTH, 0 );
		add( titlebar );

		Highlighter hl = new Highlighter( getWayDesc(way1, way2) );
		
		Text normal = PixelScene.createMultiline( hl.text, GuiProperties.regularFontSize() );
		if (hl.isHighlighted()) {
			normal.mask = hl.inverted();
		}
		normal.maxWidth(WIDTH);
		normal.measure();
		normal.x = titlebar.left();
		normal.y = titlebar.bottom() + GAP;
		add( normal );
		
		if (hl.isHighlighted()) {
			
			Text highlighted = PixelScene.createMultiline( hl.text, GuiProperties.regularFontSize() );
			highlighted.mask = hl.mask;
			
			highlighted.maxWidth(normal.getMaxWidth());
			highlighted.measure();
			highlighted.x = normal.x;
			highlighted.y = normal.y;
			add( highlighted );
	
			highlighted.hardlight( TITLE_COLOR );
		}

		RedButton btnWay1 = new RedButton( Utils.capitalize( way1.title() ) ) {
			@Override
			protected void onClick() {
				hide();
				item.choose( way1 );
			}
		};
		btnWay1.setRect( 0, normal.y + normal.height() + GAP, (WIDTH - GAP) / 2, BTN_HEIGHT );
		add( btnWay1 );
		bottom = btnWay1.bottom();
		if (way2 != null){
			RedButton btnWay2 = new RedButton( Utils.capitalize( way2.title() ) ) {
				@Override
				protected void onClick() {
					hide();
					item.choose( way2 );
				}
			};

			btnWay2.setRect( btnWay1.right() + GAP, btnWay1.top(), btnWay1.width(), BTN_HEIGHT );
			add( btnWay2 );
			bottom = btnWay2.bottom();
		}
		RedButton btnCancel = new RedButton( TXT_CANCEL ) {
			@Override
			protected void onClick() {
				hide();
			}
		};
		btnCancel.setRect( 0, bottom + GAP, WIDTH, BTN_HEIGHT );
		add( btnCancel );
		
		resize( WIDTH, (int)btnCancel.bottom() );
	}
}
