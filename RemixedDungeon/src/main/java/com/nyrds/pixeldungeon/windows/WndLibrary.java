package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.items.common.Library;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.TextButton;
import com.watabou.pixeldungeon.ui.Window;

import java.util.HashMap;
import java.util.Map;

public class WndLibrary extends Window {

	private static final int BTN_HEIGHT	= 18;
	private static final int BTN_WIDTH	= 38;
	private static final int WIDTH		= 100;
	private static final int GAP		= 2;

	private static Map<String,String> categoriesMap = new HashMap<>();

	static {
        categoriesMap.put(Library.ITEM, StringsManager.getVar(R.string.WndLibrary_Items_Btn));
        categoriesMap.put(Library.MOB, StringsManager.getVar(R.string.WndLibrary_Mobs_Btn));
		/*
		categoriesMap.put(Library.CODEX,Game.getVar(R.string.WndLibrary_Codex_Btn));
		categoriesMap.put(Library.CHAPTERS,Game.getVar(R.string.WndLibrary_Levels_Btn));
		*/

	}

	public WndLibrary() {
		super();

		//Title text
        Text tfTitle = PixelScene.createMultiline(StringsManager.getVar(R.string.WndLibrary_Catalogue_Title), GuiProperties.mediumTitleFontSize());
		tfTitle.hardlight(TITLE_COLOR);
		tfTitle.maxWidth(WIDTH - GAP);
		tfTitle.setX((WIDTH - tfTitle.width())/2);
		tfTitle.setY(GAP);
		add(tfTitle);

		//Instruction text
        Text message = PixelScene.createMultiline(StringsManager.getVar(R.string.WndLibrary_Catalogue_Instruction), GuiProperties.mediumTitleFontSize() );
		message.maxWidth(WIDTH);
		message.setY(tfTitle.bottom()+ GAP);
		add( message );

		int buttonY = (int) message.bottom()+ GAP;

		int i = 0;
		for(final Map.Entry<String,String> entry:categoriesMap.entrySet()) {

			if(!Library.isValidCategory(entry.getKey())) {
				continue;
			}

			TextButton browse = new RedButton(entry.getValue()) {
				@Override
				protected void onClick() {
					super.onClick();
					hide();
					GameScene.show(new WndLibraryCatalogue(entry.getKey(),entry.getValue()));
				}
			};
			int w;

			if(categoriesMap.size() == 2){
				w = (int) ( (WIDTH / 2) - (BTN_WIDTH * 0.5));
			}else {

				w = (int) ((WIDTH / 2) - (BTN_WIDTH * 1.1) - GAP);

				if (i >= 2) {
					w = (WIDTH / 2) + (BTN_WIDTH / 10) + GAP;
				}
			}
			if (i == 2) {
				buttonY = buttonY - (BTN_HEIGHT + GAP * 2) * 2;
			}

			browse.setRect(w, buttonY + GAP * 2, BTN_WIDTH,  BTN_HEIGHT);
			add(browse);
			buttonY = (int) browse.bottom();
			i++;
		}

		//Back Button
        TextButton back = new RedButton(StringsManager.getVar(R.string.Wnd_Button_Back)) {
			@Override
			protected void onClick() {
				super.onClick();
				hide();
			}
		};

		back.setRect((WIDTH / 2) - (BTN_WIDTH / 2) , BTN_HEIGHT / 2 + GAP * 2 + buttonY, BTN_WIDTH, BTN_HEIGHT);
		add(back);

		resize( WIDTH, (int) back.bottom() + BTN_HEIGHT / 2);
	}
}
