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

import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.ui.CheckBox;
import com.watabou.pixeldungeon.ui.Window;

public class WndSettingsCommon extends Window {

	protected static final int WIDTH      = 112;
	protected static final int BTN_HEIGHT = 18;

	protected float curY = 0;

	public WndSettingsCommon() {
		super();

		curY += SMALL_GAP;

		CheckBox btnMusic = new CheckBox(Game
				.getVar(R.string.WndSettings_Music)) {
			@Override
			protected void onClick() {
				super.onClick();
				PixelDungeon.music(checked());
			}
		};
		btnMusic.setRect(0, curY, WIDTH, BTN_HEIGHT);
		btnMusic.checked(PixelDungeon.music());
		add(btnMusic);

		CheckBox btnSound = new CheckBox(Game
				.getVar(R.string.WndSettings_Sound)) {
			@Override
			protected void onClick() {
				super.onClick();
				PixelDungeon.soundFx(checked());
				Sample.INSTANCE.play(Assets.SND_CLICK);
			}
		};
		btnSound.setRect(0, btnMusic.bottom() + SMALL_GAP, WIDTH, BTN_HEIGHT);
		btnSound.checked(PixelDungeon.soundFx());
		add(btnSound);

		curY = btnSound.bottom() + SMALL_GAP;
	}

	@Override
	public void onBackPressed() {
		hide();
	}
}
