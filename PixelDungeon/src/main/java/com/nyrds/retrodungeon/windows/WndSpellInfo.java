package com.nyrds.retrodungeon.windows;

import com.nyrds.android.util.GuiProperties;
import com.nyrds.retrodungeon.mechanics.spells.Spell;
import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.TextButton;
import com.watabou.pixeldungeon.ui.Window;

public class WndSpellInfo extends Window {

	protected static final int BTN_HEIGHT	= 18;
	protected static final int WIDTH		= 100;
	protected static final int GAP		= 2;

	protected static final String BTN_BACK = Game.getVar(R.string.Wnd_Button_Back);

	protected String getDesc(){
		return Game.getVar(R.string.WndPortal_Info);
	}

	public WndSpellInfo(final Hero hero, final Spell spell ) {
		super();

		//Title text
		Text tfTitle = PixelScene.createMultiline(spell.name(), GuiProperties.mediumTitleFontSize());
		tfTitle.hardlight(TITLE_COLOR);
		tfTitle.maxWidth(WIDTH - GAP);
		tfTitle.measure();
		tfTitle.x = (WIDTH - tfTitle.width())/2;
		tfTitle.y = GAP;
		add(tfTitle);

		//Info text
		Text info = PixelScene.createMultiline(spell.desc(), GuiProperties.regularFontSize() );
		info.maxWidth(WIDTH);
		info.measure();
		info.y = tfTitle.bottom()+ GAP;
		add( info );

		int buttonY = (int) info.bottom()+ GAP;

		//Back Button
		TextButton btnBack = new RedButton(BTN_BACK) {
			@Override
			protected void onClick() {
				super.onClick();
				hide();
			}
		};

		btnBack.setRect(0, info.bottom() + GAP, WIDTH, BTN_HEIGHT);
		add(btnBack);

		resize( WIDTH, (int) btnBack.bottom() + BTN_HEIGHT / 2);
	}
}
