package com.nyrds.pixeldungeon.windows;

import com.nyrds.android.util.GuiProperties;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.TextButton;
import com.watabou.pixeldungeon.ui.Window;

public class WndLibrary extends Window {

	private static final int BTN_HEIGHT	= 18;
	private static final int BTN_WIDTH	= 38;
	private static final int WIDTH		= 112;
	private static final int GAP		= 2;

	String[] buttonLabels = {
			Game.getVar(R.string.WndLibrary_Mobs_Btn),
			Game.getVar(R.string.WndLibrary_Items_Btn),
			Game.getVar(R.string.WndLibrary_Codex_Btn),
			Game.getVar(R.string.WndLibrary_Levels_Btn)
	};

	public WndLibrary() {
		super();
		final Window context = this;

		int h = 0;

		//Title text
		Text tfTitle = PixelScene.createMultiline(Game.getVar(R.string.WndLibrary_Catalogue_Title), GuiProperties.mediumTitleFontSize());
		tfTitle.hardlight(TITLE_COLOR);
		tfTitle.maxWidth(WIDTH - GAP);
		tfTitle.measure();
		tfTitle.x = (WIDTH - tfTitle.width())/2;
		tfTitle.y = GAP;
		add(tfTitle);

		h = h + (int) tfTitle.bottom()+ GAP;

		//Instruction text
		Text message = PixelScene.createMultiline( Game.getVar(R.string.WndLibrary_Catalogue_Instruction), GuiProperties.mediumTitleFontSize() );
		message.maxWidth(WIDTH);
		message.measure();
		message.y = tfTitle.bottom()+ GAP;
		add( message );

		h = h + (int) message.bottom()+ GAP;

		int buttonY = h;

		//Button maker
		for (int i = 0; i < buttonLabels.length; i++){

			//Browse Button
			TextButton browse = new RedButton(Game.getVar(R.string.WndLibrary_Catalogue_Btn)) {
				@Override
				protected void onClick() {
					super.onClick();
					context.hide();
				}
			};

			browse.setRect((WIDTH / 2) - (BTN_WIDTH / 2), buttonY + GAP, BTN_WIDTH,  BTN_HEIGHT);
			add(browse);
			buttonY = (int) browse.bottom();
			h = h + (int) browse.bottom()+ GAP;
		}

		//Back Button
		TextButton back = new RedButton(Game.getVar(R.string.Wnd_Button_Back)) {
			@Override
			protected void onClick() {
				super.onClick();
				context.hide();
			}
		};

		back.setRect((WIDTH / 2) - (BTN_WIDTH / 2) , BTN_HEIGHT + GAP + buttonY, BTN_WIDTH, BTN_HEIGHT);
		add(back);

		h = h + (int) back.bottom()+ GAP;
		resize( WIDTH,  170);
	}
}
