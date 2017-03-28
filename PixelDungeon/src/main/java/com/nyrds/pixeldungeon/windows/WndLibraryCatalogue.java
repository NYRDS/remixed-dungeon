package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.TextButton;
import com.watabou.pixeldungeon.ui.Window;

public class WndLibraryCatalogue extends Window {

	private static final int BTN_HEIGHT	= 18;
	private static final int BTN_WIDTH	= 38;
	private static final int WIDTH		= 112;
	private static final int GAP		= 4;

	public WndLibraryCatalogue() {
		super();
	final Window context = this;

	//Browse Button
	TextButton browse = new RedButton(Game.getVar(R.string.WndLibrary_Catalogue_Btn)) {
		@Override
		protected void onClick() {
			super.onClick();
			context.hide();
		}
	};

	browse.setRect(GAP, GAP, BTN_WIDTH + GAP, BTN_HEIGHT + GAP);

	int w = (int) browse.right()+ GAP;
	int h = (int) browse.bottom()+ GAP;

	add(browse);

	//Back Button
		TextButton back = new RedButton(Game.getVar(R.string.Wnd_Button_Back)) {
			@Override
			protected void onClick() {
				super.onClick();
				context.hide();
			}
		};

		back.setRect(WIDTH - GAP - BTN_WIDTH, GAP, WIDTH - GAP, BTN_HEIGHT + GAP);

		w = w + (int) back.right()+ GAP;
		h = h + (int) back.bottom()+ GAP;

		add(back);

		resize( WIDTH,  h);
	}
}
