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
import com.nyrds.pixeldungeon.windows.VBox;
import com.nyrds.pixeldungeon.windows.WndInGameUiSettings;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.scenes.PixelScene;

public class WndSettingsInGame extends WndSettingsCommon {

	public WndSettingsInGame() {
		super();
		VBox menuItems = new VBox();

		addSoundControls(menuItems);

		menuItems.add( new MenuCheckBox(Game
				.getVar(R.string.WndSettings_Brightness),PixelDungeon.brightness()) {
			@Override
			protected void onClick() {
				super.onClick();
				PixelDungeon.brightness(checked());
			}
		});

		menuItems.add(createZoomButtons());

		menuItems.add(new MenuButton(Game
				.getVar(R.string.WndSettings_InGameUiSettings)){
			@Override
			protected void onClick() {
				super.onClick();
				WndSettingsInGame.this.add(new WndInGameUiSettings());
			}
		});

		menuItems.setRect(0,0,WIDTH,menuItems.childsHeight());

		add(menuItems);

		resize(WIDTH, (int) menuItems.childsHeight());
	}

	private Selector createZoomButtons() {
		return new Selector(WIDTH, BTN_HEIGHT, Game
				.getVar(R.string.WndSettings_ZoomDef), new Selector.PlusMinusDefault() {

			@Override
			public void onPlus(Selector s) {
				zoom(Camera.main.zoom + 0.1f,s);
			}

			@Override
			public void onMinus(Selector s) {
				zoom(Camera.main.zoom - 0.1f,s);
			}

			@Override
			public void onDefault(Selector s) {
				zoom(PixelScene.defaultZoom,s);
			}

			private void zoom(float value,Selector s) {
				Camera.main.zoom(value);
				PixelDungeon.zoom(value - PixelScene.defaultZoom);

				float zoom = Camera.main.zoom;
				s.enable(zoom < PixelScene.maxZoom, zoom > PixelScene.minZoom, true);
			}
		});
	}


	@Override
	public void onBackPressed() {
		super.onBackPressed();
		PixelDungeon.resetScene();
	}
}
