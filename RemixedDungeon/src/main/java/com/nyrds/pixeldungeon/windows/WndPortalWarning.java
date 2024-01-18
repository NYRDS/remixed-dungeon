package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.util.GuiProperties;
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
        Text tfTitle = PixelScene.createMultiline(R.string.WndPortal_Warning_Title, GuiProperties.mediumTitleFontSize());
		tfTitle.hardlight(TITLE_COLOR);
		tfTitle.maxWidth(WIDTH - GAP);
		tfTitle.setX((WIDTH - tfTitle.width())/2);
		tfTitle.setY(GAP);
		add(tfTitle);

		//Instruction text
        Text message = PixelScene.createMultiline(R.string.WndPortal_Warning_Info, GuiProperties.regularFontSize() );
		message.maxWidth(WIDTH);
		message.setY(tfTitle.bottom()+ GAP);
		add( message );

		int buttonY = (int) message.bottom()+ GAP;


		//Yes Button
        TextButton btnYes = new RedButton(R.string.Wnd_Button_Yes) {
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
        TextButton btnNo = new RedButton(R.string.Wnd_Button_No) {
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
