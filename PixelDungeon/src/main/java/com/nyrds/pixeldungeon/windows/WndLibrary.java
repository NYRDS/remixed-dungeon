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

public class WndLibrary extends Window {

	private static final int BTN_HEIGHT	= 18;
	private static final int BTN_WIDTH	= 38;
	private static final int WIDTH		= 100;
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

		//Title text
		Text tfTitle = PixelScene.createMultiline(Game.getVar(R.string.WndLibrary_Catalogue_Title), GuiProperties.mediumTitleFontSize());
		tfTitle.hardlight(TITLE_COLOR);
		tfTitle.maxWidth(WIDTH - GAP);
		tfTitle.measure();
		tfTitle.x = (WIDTH - tfTitle.width())/2;
		tfTitle.y = GAP;
		add(tfTitle);

		//Instruction text
		Text message = PixelScene.createMultiline( Game.getVar(R.string.WndLibrary_Catalogue_Instruction), GuiProperties.mediumTitleFontSize() );
		message.maxWidth(WIDTH);
		message.measure();
		message.y = tfTitle.bottom()+ GAP;
		add( message );

		int buttonY = (int) message.bottom()+ GAP;

		//Button maker
		for (int i = 0; i < buttonLabels.length; i++){

			final int catalogueNumber = i;
			TextButton browse = new RedButton(buttonLabels[i]) {
				@Override
				protected void onClick() {
					super.onClick();
					context.hide();
					GameScene.show(new WndLibraryCatalogue(catalogueNumber, buttonLabels[catalogueNumber]));
				}
			};

			int w = (int) ((WIDTH / 2) - (BTN_WIDTH * 1.1) - GAP);
			if (i >= 2){
				w = (WIDTH / 2) + (BTN_WIDTH/10) + GAP;
			}
			if (i == 2) {
				buttonY = buttonY - (BTN_HEIGHT + GAP * 2) * 2;
			}

			browse.setRect(w, buttonY + GAP * 2, BTN_WIDTH,  BTN_HEIGHT);
			add(browse);
			buttonY = (int) browse.bottom();
		}

		//Back Button
		TextButton back = new RedButton(Game.getVar(R.string.Wnd_Button_Back)) {
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
