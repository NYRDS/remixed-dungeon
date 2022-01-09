/*
 * Pixel Dungeon
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.watabou.pixeldungeon.windows;

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
		tfTitle.x = tfTitle.y = GAP;
		tfTitle.maxWidth(maxW);
		add(tfTitle);

        Text pleaseHelpTranslate = PixelScene.createMultiline(StringsManager.getVar(R.string.WndSelectLanguage_ImproveTranslation), GuiProperties.titleFontSize());
		pleaseHelpTranslate.maxWidth(maxW);
		pleaseHelpTranslate.x = GAP;
		pleaseHelpTranslate.y = tfTitle.y + tfTitle.height() + GAP;
		add(pleaseHelpTranslate);

        Text translateLink = PixelScene.createMultiline(StringsManager.getVar(R.string.WndSelectLanguage_LinkToTranslationSite), GuiProperties.titleFontSize());
		translateLink.hardlight(TITLE_COLOR);
		translateLink.maxWidth(maxW);
		translateLink.x = GAP;
		translateLink.y = pleaseHelpTranslate.y + pleaseHelpTranslate.height() + GAP;
		add(translateLink);

		TouchArea area = new TouchArea(translateLink) {
			@Override
			protected void onClick(Touchscreen.Touch touch) {
				Game.instance().openUrl(StringsManager.getVar(R.string.WndSelectLanguage_TranslationLink),StringsManager.getVar(R.string.WndSelectLanguage_TranslationLink));
			}
		};
		add(area);

		float pos = translateLink.y + translateLink.height() + GAP;

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
