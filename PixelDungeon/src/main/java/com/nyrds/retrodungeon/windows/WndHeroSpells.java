package com.nyrds.retrodungeon.windows;

import com.nyrds.android.util.GuiProperties;
import com.nyrds.retrodungeon.mechanics.spells.Spell;
import com.nyrds.retrodungeon.mechanics.spells.SpellFactory;
import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.Text;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.Icons;
import com.watabou.pixeldungeon.ui.ScrollPane;
import com.watabou.pixeldungeon.ui.SimpleButton;
import com.watabou.pixeldungeon.ui.Window;

import java.util.ArrayList;

public class WndHeroSpells extends Window {

	private static final String TXT_TITLE   = Game.getVar(R.string.WndSpells_Title);
	private static final String TXT_LVL   = Game.getVar(R.string.WndSpells_Level);

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

		Text txtLvl = PixelScene.createText(TXT_LVL + hero.magicLvl(), GuiProperties.titleFontSize());
		txtLvl.hardlight(Window.TITLE_COLOR);
		txtLvl.measure();
		txtLvl.x = width - txtLvl.width();
		add(txtLvl);

		ScrollPane list = new ScrollableList(new Component());

		add(list);

		list.setRect(0, txtTitle.height(), width, height - txtTitle.height());

		String affinity = hero.heroClass.getMagicAffinity();

		float yPos = txtTitle.bottom() + MARGIN;

		ArrayList<String> spells = SpellFactory.getSpellsByAffinity(affinity);
		if(spells != null) {
			int i = 1;
			for (String spell : spells) {
				yPos = addSpell(spell, hero, yPos, i);
				i++;
			}
		}
	}

	private float addSpell(String spellName ,final Hero hero,  float yPos, int i) {

		final Spell spell = SpellFactory.getSpellByName(spellName);
		if(spell == null || spell.level() > hero.magicLvl()) {
			return yPos;
		}
		int xPos = 0;
		if ( i % 2 == 0 ) {
			xPos = width - 48 - MARGIN * 2;
		}

		float yPosTemp = yPos;

		Text txtName;

		txtName = PixelScene.createText(spell.name(), GuiProperties.titleFontSize());
		txtName.measure();
		txtName.x = xPos;
		txtName.y = yPos;
		add(txtName);

		Image icon = spell.image();

		icon.frame( spell.film.get( spell.imageIndex ) );
		icon.y = txtName.bottom();
		icon.x = xPos;
		add( icon );

		SimpleButton btnCast = new SimpleButton(Icons.get(Icons.BTN_TARGET)) {
			protected void onClick() {
				hide();
				spell.cast(hero);
			}
		};
		btnCast.setRect(
				icon.x + icon.width() + MARGIN, icon.y,
				16, 15 );
		add( btnCast );

		SimpleButton btnInfo = new SimpleButton(Icons.get(Icons.BTN_QUESTION)) {
			protected void onClick() {
				hide();
				GameScene.show( new WndSpellInfo(hero, spell) );
			}
		};
		btnInfo.setRect(
				icon.x + icon.width() + MARGIN, btnCast.bottom() + MARGIN,
				16, 15 );
		add( btnInfo );

		Text txtCost;

		txtCost = PixelScene.createText( Game.getVar(R.string.Mana_Cost) + spell.spellCost(), GuiProperties.titleFontSize());
		txtCost.measure();
		txtCost.x = xPos;
		txtCost.y = icon.bottom();
		add(txtCost);

		if ( xPos == 0 ){
			return yPosTemp;
		}

		return txtCost.bottom() + MARGIN;
	}
}
