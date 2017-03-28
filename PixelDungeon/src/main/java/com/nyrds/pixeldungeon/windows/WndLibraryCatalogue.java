package com.nyrds.pixeldungeon.windows;

import com.nyrds.android.util.GuiProperties;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.TextButton;
import com.watabou.pixeldungeon.ui.Window;

public class WndLibraryCatalogue extends Window {

	private static final int BTN_HEIGHT	= 18;
	private static final int BTN_WIDTH	= 38;
	private static final int WIDTH		= 112;
	private static final int GAP		= 4;

	public WndLibraryCatalogue(int catalogueNumber, String catalogueName) {
		super();
		final Window context = this;

		//Title text
		Text tfTitle = PixelScene.createMultiline(catalogueName, GuiProperties.mediumTitleFontSize());
		tfTitle.hardlight(TITLE_COLOR);
		tfTitle.maxWidth(WIDTH - GAP);
		tfTitle.measure();
		tfTitle.x = (WIDTH - tfTitle.width())/2;
		tfTitle.y = GAP;
		add(tfTitle);

		//Instruction text
		Text message = PixelScene.createMultiline( "This is page of catalogue number: " + catalogueNumber + ". Named: " + catalogueName, GuiProperties.mediumTitleFontSize() );
		message.maxWidth(WIDTH);
		message.measure();
		message.y = tfTitle.bottom()+ GAP;
		add( message );

		//Back Button
		TextButton back = new RedButton(Game.getVar(R.string.Wnd_Button_Back)) {
			@Override
			protected void onClick() {
				super.onClick();
				context.hide();
				GameScene.show(new WndLibrary());
			}
		};

		back.setRect(GAP, (int) message.bottom()+ GAP, BTN_WIDTH + GAP, BTN_HEIGHT + GAP);
		int h = (int) back.bottom()+ GAP;

		add(back);

		resize( WIDTH,  h);
	}
}
