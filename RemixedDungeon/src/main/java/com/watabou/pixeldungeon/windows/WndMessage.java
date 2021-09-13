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

import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.Window;

public class WndMessage extends Window {

	private static final int WIDTH = 120;
	private final Text info;

	public WndMessage(String text) {

		super();

		info = PixelScene.createMultiline(text, GuiProperties.regularFontSize());
		info.maxWidth(WIDTH - MARGIN * 2);
		info.x = info.y = MARGIN;
		add(info);

		resize(
				(int) info.width() + MARGIN * 2,
				(int) info.height() + MARGIN * 2);
	}

	public void setText(String text) {
		info.text(text);
		resize(
				(int) info.width() + MARGIN * 2,
				(int) info.height() + MARGIN * 2);
	}
}
