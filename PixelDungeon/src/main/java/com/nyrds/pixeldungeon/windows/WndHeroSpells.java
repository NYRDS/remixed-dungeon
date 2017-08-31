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
import com.nyrds.pixeldungeon.mechanics.spells.Spell;
import com.nyrds.pixeldungeon.mechanics.spells.SummonDeathling;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.Text;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.CatalogusListItem;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.ScrollPane;
import com.watabou.pixeldungeon.ui.Window;

import java.util.ArrayList;

public class WndHeroSpells extends Window {

	private static final String TXT_TITLE   = Game.getVar(R.string.WndSpells_Title);
	private static final String TXT_INFO   = Game.getVar(R.string.WndSpells_Info);
	private static final String TXT_USE   = Game.getVar(R.string.WndSpells_Use);

	private static final int MARGIN = 2;

	private Text       txtTitle;
	private ScrollPane list;

	private ArrayList<CatalogusListItem> items = new ArrayList<>();

	public WndHeroSpells() {
		super();

		final Hero hero = Dungeon.hero;
		resize(WndHelper.getLimitedWidth(120), WndHelper.getFullscreenHeight() - MARGIN);

		txtTitle = PixelScene.createText(TXT_TITLE, GuiProperties.titleFontSize());
		txtTitle.hardlight(Window.TITLE_COLOR);
		txtTitle.measure();
		add(txtTitle);

		list = new ScrollableList(new Component());

		add(list);

		list.setRect(0, txtTitle.height(), width, height - txtTitle.height());

		String affinity = hero.heroClass.getMagicAffinity();

		float yPos = txtTitle.bottom() + MARGIN;

		final Spell spell = new SummonDeathling();

		Image icon = spell.image();

		icon.frame( spell.film.get( spell.imageIndex ) );
		icon.y = yPos;
		add( icon );

		RedButton btnCast = new RedButton( TXT_USE ) {
			@Override
			protected void onClick() {
				hide();
				spell.use(hero);
			}
		};
		btnCast.setRect(
				icon.width() + MARGIN, icon.y,
				btnCast.reqWidth() + 2, btnCast.reqHeight() + 2 );
		add( btnCast );

		RedButton btnInfo = new RedButton( TXT_INFO ) {
			@Override
			protected void onClick() {
				hide();
				GameScene.show( new WndSpellInfo(hero, spell) );
			}
		};
		btnInfo.setRect(
				btnCast.right() + MARGIN, icon.y,
				btnInfo.reqWidth() + 2, btnInfo.reqHeight() + 2 );
		add( btnInfo );


		Text txtName;

		txtName = PixelScene.createText(spell.name(), GuiProperties.titleFontSize());
		txtName.measure();
		txtName.x = btnCast.left() + MARGIN;
		txtName.y = btnCast.bottom() + MARGIN;
		add(txtName);

		yPos = icon.bottom();
	}

}
