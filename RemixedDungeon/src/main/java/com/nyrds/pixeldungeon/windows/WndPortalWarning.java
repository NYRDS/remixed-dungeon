package com.nyrds.pixeldungeon.windows;

import com.nyrds.android.util.GuiProperties;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.TextButton;
import com.watabou.pixeldungeon.ui.Window;

public class WndPortalWarning extends Window {

	private static final int BTN_HEIGHT	= 18;
	private static final int BTN_WIDTH	= 38;
	private static final int WIDTH		= 100;
	private static final int GAP		= 2;

	public WndPortalWarning() {
		super();

		//Title text
		Text tfTitle = PixelScene.createMultiline(Game.getVar(R.string.WndPortal_Warning_Title), GuiProperties.mediumTitleFontSize());
		tfTitle.hardlight(TITLE_COLOR);
		tfTitle.maxWidth(WIDTH - GAP);
		tfTitle.x = (WIDTH - tfTitle.width())/2;
		tfTitle.y = GAP;
		add(tfTitle);

		//Instruction text
		Text message = PixelScene.createMultiline(Game.getVar(R.string.WndPortal_Warning_Info), GuiProperties.regularFontSize() );
		message.maxWidth(WIDTH);
		message.y = tfTitle.bottom()+ GAP;
		add( message );

		int buttonY = (int) message.bottom()+ GAP;


		//Yes Button
		TextButton btnYes = new RedButton(Game.getVar(R.string.Wnd_Button_Yes)) {
			@Override
			protected void onClick() {
				super.onClick();
				hide();
			}
		};

		btnYes.setRect((WIDTH / 2) - (BTN_WIDTH / 2) , BTN_HEIGHT / 2 + GAP * 2 + buttonY, BTN_WIDTH, BTN_HEIGHT);
		add(btnYes);

		buttonY = (int) btnYes.bottom()+ GAP;

		//No Button
		TextButton btnNo = new RedButton(Game.getVar(R.string.Wnd_Button_No)) {
			@Override
			protected void onClick() {
				super.onClick();
				hide();
			}
		};

		btnNo.setRect((WIDTH / 2) - (BTN_WIDTH / 2) , GAP * 2 + buttonY, BTN_WIDTH, BTN_HEIGHT);
		add(btnNo);

		resize( WIDTH, (int) btnNo.bottom() + BTN_HEIGHT / 2);
	}
}
