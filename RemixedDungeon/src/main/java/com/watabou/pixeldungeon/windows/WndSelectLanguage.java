
package com.watabou.pixeldungeon.windows;

import android.content.Intent;
import android.net.Uri;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.windows.WndHelper;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.game.RemixedDungeon;
import com.nyrds.platform.input.Touchscreen;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Text;
import com.watabou.noosa.TouchArea;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.SystemRedButton;
import com.watabou.pixeldungeon.ui.Window;

public class WndSelectLanguage extends Window {

	public WndSelectLanguage(String title, String... options) {
		super();

		int WIDTH = WndHelper.getFullscreenWidth();

		int maxW = WIDTH - GAP * 2;

		Text tfTitle = PixelScene.createMultiline(title, GuiProperties.titleFontSize());
		tfTitle.hardlight(TITLE_COLOR);
		tfTitle.setY(GAP);
		tfTitle.setX(GAP);
		tfTitle.maxWidth(maxW);
		add(tfTitle);

        Text pleaseHelpTranslate = PixelScene.createMultiline(R.string.WndSelectLanguage_ImproveTranslation, GuiProperties.titleFontSize());
		pleaseHelpTranslate.maxWidth(maxW);
		pleaseHelpTranslate.setX(GAP);
		pleaseHelpTranslate.setY(tfTitle.getY() + tfTitle.height() + GAP);
		add(pleaseHelpTranslate);

        Text translateLink = PixelScene.createMultiline(R.string.WndSelectLanguage_LinkToTranslationSite, GuiProperties.titleFontSize());
		translateLink.hardlight(TITLE_COLOR);
		translateLink.maxWidth(maxW);
		translateLink.setX(GAP);
		translateLink.setY(pleaseHelpTranslate.getY() + pleaseHelpTranslate.height() + GAP);
		add(translateLink);

		TouchArea area = new TouchArea(translateLink) {
			@Override
			protected void onClick(Touchscreen.Touch touch) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(StringsManager.getVar(R.string.WndSelectLanguage_TranslationLink)));

                Game.instance().startActivity(Intent.createChooser(intent, StringsManager.getVar(R.string.WndSelectLanguage_TranslationLink)));
			}
		};
		add(area);

		float pos = translateLink.getY() + translateLink.height() + GAP;

		final int columns = RemixedDungeon.landscape() ? 3 : 2;

		int BUTTON_WIDTH = WIDTH / columns - GAP;

		int lastButtonBottom = 0;

		for (int i = 0; i < options.length / columns + 1; i++) {

			for (int j = 0; j < columns; j++) {
				final int index = i * columns + j;
				if (!(index < options.length)) {
					break;
				}
				SystemRedButton btn = new SystemRedButton(options[index]) {
					@Override
					protected void onClick() {
						hide();
						onSelect(index);
					}
				};

				btn.setRect(GAP + j * (BUTTON_WIDTH + GAP), pos, BUTTON_WIDTH, BUTTON_HEIGHT);
				add(btn);

				lastButtonBottom = (int) btn.bottom();
			}
			pos += BUTTON_HEIGHT;
		}

		resize(WIDTH, lastButtonBottom + GAP);
	}

	protected void onSelect(int index) {
	}
}
