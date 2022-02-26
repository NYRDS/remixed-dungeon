package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.TextButton;
import com.watabou.pixeldungeon.ui.Window;

public class WndLibraryEntry extends Window {

	private static final int BTN_HEIGHT	= 18;
	private static final int BTN_WIDTH	= 38;
	private static final int WIDTH		= 100;
	private static final int GAP		= 2;

	public WndLibraryEntry(String entry) {
		super();
		final Window context = this;

		//Title text
		Text tfTitle = PixelScene.createMultiline(entry, GuiProperties.mediumTitleFontSize());
		tfTitle.hardlight(TITLE_COLOR);
		tfTitle.maxWidth(WIDTH - GAP);
		tfTitle.setX((WIDTH - tfTitle.width())/2);
		tfTitle.setY(GAP);
		add(tfTitle);

		//Entry text
		Text message = PixelScene.createMultiline( entry, GuiProperties.regularFontSize() );
		message.maxWidth(WIDTH);
		message.setY(tfTitle.bottom()+ GAP);
		add( message );

		int buttonY = (int) message.bottom()+ GAP;

		//Back Button
        TextButton back = new RedButton(StringsManager.getVar(R.string.Wnd_Button_Back)) {
			@Override
			protected void onClick() {
				super.onClick();
				context.hide();
			}
		};

		back.setRect((WIDTH / 2) - (BTN_WIDTH / 2) , BTN_HEIGHT / 2 + GAP * 2 + buttonY, BTN_WIDTH, BTN_HEIGHT);
		add(back);

		resize( WIDTH, (int) back.bottom() + BTN_HEIGHT / 2);
	}
}
