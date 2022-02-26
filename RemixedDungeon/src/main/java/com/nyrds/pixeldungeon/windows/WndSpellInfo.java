package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.mechanics.spells.Spell;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.ml.actions.UseSpell;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Image;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.Icons;
import com.watabou.pixeldungeon.ui.SimpleButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.windows.IconTitle;

import org.jetbrains.annotations.NotNull;

public class WndSpellInfo extends Window {

	protected static final int BTN_HEIGHT	= 18;
	protected static final int WIDTH		= 100;
	protected static final int GAP		    = 2;

	public WndSpellInfo(@NotNull final WndHeroSpells owner, final Hero hero, final Spell spell ) {
		super();

		IconTitle title = new IconTitle(new Image(spell.image(hero)), spell.name());
		title.setRect(0,0,WIDTH,0);
		add(title);

		//Info text
		Text info = PixelScene.createMultiline(spell.desc(), GuiProperties.regularFontSize() );
		info.maxWidth(WIDTH);
		info.setY(title.bottom()+ GAP);
		add( info );

		Text txtCost;

        txtCost = PixelScene.createText( StringsManager.getVar(R.string.Mana_Cost) + spell.spellCost(), GuiProperties.regularFontSize());
		txtCost.setX(0);
		txtCost.setY(info.bottom() + GAP);
		add(txtCost);

		SimpleButton btnCast = new SimpleButton(Icons.get(Icons.BTN_TARGET)) {
			protected void onClick() {
				hide();
				owner.hide();
				hero.nextAction(new UseSpell(spell));
			}
		};

		btnCast.setPos(0, txtCost.bottom() + GAP);
		add(btnCast);

		resize( WIDTH, (int) btnCast.bottom() + GAP);
	}
}
