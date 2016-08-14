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

import android.content.Intent;
import android.net.Uri;

import com.nyrds.android.util.GuiProperties;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.input.Touchscreen;
import com.watabou.noosa.Game;
import com.watabou.noosa.Text;
import com.watabou.noosa.TouchArea;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.SystemRedButton;
import com.watabou.pixeldungeon.ui.Window;

public class WndSelectLanguage extends Window {

	private static final int WIDTH			= 120;
	private static final int MARGIN 		= 2;
	private static final int BUTTON_HEIGHT	= 20;
	private static final int BUTTON_WIDTH	= 58;

	public WndSelectLanguage(String title, String... options) {
		super();

		int maxW = WIDTH - MARGIN * 2;

		Text tfTitle = PixelScene.createMultiline(title, GuiProperties.titleFontSize());
		tfTitle.hardlight(TITLE_COLOR);
		tfTitle.x = tfTitle.y = MARGIN;
		tfTitle.maxWidth(maxW);
		tfTitle.measure();
		add(tfTitle);

		Text pleaseHelpTranslate = PixelScene.createMultiline(Game.getVar(R.string.WndSelectLanguage_ImproveTranslation), GuiProperties.titleFontSize());
		pleaseHelpTranslate.maxWidth(maxW);
		pleaseHelpTranslate.measure();
		pleaseHelpTranslate.x = MARGIN;
		pleaseHelpTranslate.y = tfTitle.y + tfTitle.height() + MARGIN;
		add(pleaseHelpTranslate);

		Text translateLink=PixelScene.createMultiline(Game.getVar(R.string.WndSelectLanguage_LinkToTranslationSite), GuiProperties.titleFontSize());
		translateLink.hardlight(TITLE_COLOR);
		translateLink.maxWidth(maxW);
		translateLink.measure();
		translateLink.x = MARGIN;
		translateLink.y = pleaseHelpTranslate.y + pleaseHelpTranslate.height() + MARGIN;
		add( translateLink );

		TouchArea area = new TouchArea( translateLink ) {
			@Override
			protected void onClick( Touchscreen.Touch touch ) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Game.getVar(R.string.WndSelectLanguage_TranslationLink)));

				Game.instance().startActivity( Intent.createChooser(intent, Game.getVar(R.string.WndSelectLanguage_TranslationLink)) );
			}
		};
		add(area);

		float pos = translateLink.y + translateLink.height() + MARGIN;

		for (int i = 0; i < options.length / 2 + 1; i++) {
			for(int j =0;j<2;j++) {
				final int index = i*2+j;
				if(!(index<options.length)) {
					break;
				}
				SystemRedButton btn = new SystemRedButton( options[index] ) {
					@Override
					protected void onClick() {
						hide();
						onSelect( index );
					}
				};

				btn.setRect( MARGIN + j*(BUTTON_WIDTH+MARGIN), pos, BUTTON_WIDTH, BUTTON_HEIGHT );
				add( btn );
			}
			pos += BUTTON_HEIGHT + MARGIN;
		}

		resize( WIDTH, (int)pos );
	}

	protected void onSelect( int index ) {}
}
