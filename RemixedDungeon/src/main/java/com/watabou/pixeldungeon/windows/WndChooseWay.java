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

import com.nyrds.pixeldungeon.items.common.MasteryItem;
import com.nyrds.pixeldungeon.items.necropolis.BlackSkull;
import com.nyrds.pixeldungeon.items.necropolis.BlackSkullOfMastery;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.ui.Highlighter;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;

public class WndChooseWay extends Window {
	
	private static final int WIDTH		= 120;
	private static final int BTN_HEIGHT	= 18;

	public WndChooseWay(@NotNull Char chr,final Item item, final HeroSubClass way){
		super();
		chooseWay(chr, item, way, null );
	}

	public WndChooseWay(@NotNull Char chr , final Item item, final HeroSubClass way1, final HeroSubClass way2 ) {
		super();
		chooseWay(chr, item, way1, way2 );
	}

	private String getWayDesc(final HeroSubClass way1, final HeroSubClass way2){
		String desc =  way1.desc();
		if (way2 != null){
			desc = desc + "\n\n" + way2.desc();
		}
		if (way1 == HeroSubClass.LICH){
            desc = StringsManager.getVar(R.string.BlackSkullOfMastery_Title) + "\n\n"
					+ desc + "\n\n" + StringsManager.getVar(R.string.BlackSkullOfMastery_RemainHumanDesc);
		}
        desc = desc + "\n\n" + StringsManager.getVar(R.string.WndChooseWay_Message);
		return desc;
	}

	private void chooseWay(@NotNull Char chr, final Item item, final HeroSubClass way1, final HeroSubClass way2) {
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
		normal.x = titlebar.left();
		normal.y = titlebar.bottom() + GAP;
		add( normal );
		
		if (hl.isHighlighted()) {
			
			Text highlighted = PixelScene.createMultiline( hl.text, GuiProperties.regularFontSize() );
			highlighted.mask = hl.mask;
			
			highlighted.maxWidth(normal.getMaxWidth());
			highlighted.x = normal.x;
			highlighted.y = normal.y;
			add( highlighted );
	
			highlighted.hardlight( TITLE_COLOR );
		}

		RedButton btnWay1 = new RedButton( Utils.capitalize( way1.title() ) ) {
			@Override
			protected void onClick() {
				hide();
				MasteryItem.choose(chr, item, way1 );
			}
		};
		btnWay1.setRect( 0, normal.y + normal.height() + GAP, (WIDTH - GAP) / 2, BTN_HEIGHT );
		add( btnWay1 );

		if (way1 != HeroSubClass.LICH){
			RedButton btnWay2 = new RedButton( Utils.capitalize( way2.title() ) ) {
				@Override
				protected void onClick() {
					hide();
					MasteryItem.choose(chr, item, way2 );
				}
			};
			btnWay2.setRect( btnWay1.right() + GAP, btnWay1.top(), btnWay1.width(), BTN_HEIGHT );
			add( btnWay2 );
		} else {
			btnBreakSpell(btnWay1);
		}

        RedButton btnCancel = new RedButton(StringsManager.getVar(R.string.WndChooseWay_Cancel)) {
			@Override
			protected void onClick() {
				hide();
			}
		};
		btnCancel.setRect( 0, btnWay1.bottom() + GAP, WIDTH, BTN_HEIGHT );
		add( btnCancel );
		
		resize( WIDTH, (int)btnCancel.bottom() );
	}

	private void btnBreakSpell(RedButton btnWay1){
        RedButton btnWay2 = new RedButton( Utils.capitalize(StringsManager.getVar(R.string.BlackSkullOfMastery_Necromancer)) ) {
			@Override
			protected void onClick() {
				hide();
				Hero hero = Dungeon.hero;
				Item a = hero.getBelongings().getItem( BlackSkullOfMastery.class );
				a.removeItemFrom(hero);
				Item b = new BlackSkull();
				b.doDrop(hero);
			}
		};
		btnWay2.setRect( btnWay1.right() + GAP, btnWay1.top(), btnWay1.width(), BTN_HEIGHT );
		add( btnWay2 );
	}
}
