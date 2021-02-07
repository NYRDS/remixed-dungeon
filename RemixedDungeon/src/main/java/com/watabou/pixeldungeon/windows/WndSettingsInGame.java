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
import com.nyrds.pixeldungeon.windows.WndInGameUiSettings;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.RemixedDungeon;
import com.watabou.pixeldungeon.scenes.PixelScene;

public class WndSettingsInGame extends WndMenuCommon {

	@Override
	protected void createItems()
	{
		addSoundControls(menuItems);

		menuItems.add( new MenuCheckBox(Game
				.getVar(R.string.WndSettings_Brightness),RemixedDungeon.brightness()) {
			@Override
			protected void onClick() {
				super.onClick();
				RemixedDungeon.brightness(checked());
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

		if(Game.instance().playGames.isConnected()) {
			menuItems.add(new MenuButton(Game.getVar(R.string.WndSettings_RecordVideo)) {
				@Override
				protected void onClick() {
					super.onClick();
					Game.instance().playGames.showVideoOverlay();

				}
			});
		}
	}

	private Selector createZoomButtons() {
		return new Selector(WIDTH, BUTTON_HEIGHT, Game
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
				RemixedDungeon.zoom(value - PixelScene.defaultZoom);

				float zoom = Camera.main.zoom;
				s.enable(zoom < PixelScene.maxZoom, zoom > PixelScene.minZoom, true);
			}
		});
	}


	@Override
	public void onBackPressed() {
		super.onBackPressed();
		RemixedDungeon.resetScene();
	}
}
