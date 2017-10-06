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
import com.nyrds.pixeldungeon.mechanics.spells.SpellFactory;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.Text;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.ScrollPane;
import com.watabou.pixeldungeon.ui.Window;

import java.util.ArrayList;

public class WndHeroSpells extends Window {

	private static final String TXT_TITLE   = Game.getVar(R.string.WndSpells_Title);
	private static final String TXT_INFO   = Game.getVar(R.string.WndSpells_Info);
	private static final String TXT_USE   = Game.getVar(R.string.WndSpells_Use);

	private static final int MARGIN = 2;
	private static final int WINDOW_MARGIN = 10;

	public WndHeroSpells() {
		super();

		final Hero hero = Dungeon.hero;
		resize(WndHelper.getLimitedWidth(120), WndHelper.getFullscreenHeight() - WINDOW_MARGIN);

		Text txtTitle = PixelScene.createText(TXT_TITLE, GuiProperties.titleFontSize());
		txtTitle.hardlight(Window.TITLE_COLOR);
		txtTitle.measure();
		add(txtTitle);

		ScrollPane list = new ScrollableList(new Component());

		add(list);

		list.setRect(0, txtTitle.height(), width, height - txtTitle.height());

		String affinity = hero.heroClass.getMagicAffinity();

		float yPos = txtTitle.bottom() + MARGIN;

		ArrayList<String> spells = SpellFactory.getSpellsByAffinity(affinity);
		if(spells != null) {
			for (String spell : spells) {

				yPos = addSpell(spell, hero, yPos);
			}
		}
	}

	private float addSpell(String spellName ,final Hero hero,  float yPos) {

		final Spell spell = SpellFactory.getSpellByName(spellName);
		if(spell == null || spell.level() > hero.magicLvl()) {
			return yPos;
		}

		Text txtName;

		txtName = PixelScene.createText(spell.name(), GuiProperties.titleFontSize());
		txtName.measure();
		txtName.y = yPos;
		add(txtName);

		Image icon = spell.image();

		icon.frame( spell.film.get( spell.imageIndex ) );
		icon.y = txtName.bottom() + MARGIN;
		add( icon );

		RedButton btnCast = new RedButton( TXT_USE ) {
			@Override
			protected void onClick() {
				hide();
				spell.cast(hero);
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

		Text txtCost;

		txtCost = PixelScene.createText( Game.getVar(R.string.Mana_Title) + ": " + spell.spellCost(), GuiProperties.titleFontSize());
		txtCost.measure();
		txtCost.y = icon.bottom() + MARGIN;
		add(txtCost);

		return txtCost.bottom() + MARGIN;
	}
}
