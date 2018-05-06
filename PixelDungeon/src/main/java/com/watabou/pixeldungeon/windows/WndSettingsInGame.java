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
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.Preferences;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.Toolbar;
import com.watabou.pixeldungeon.windows.elements.Tool;

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
		menuItems.add(createUiZoomButtons());
		menuItems.add(createQuickSlotsSelector());
		menuItems.add(new MenuButton(Game.getVar(R.string.WndSettings_ActionButtonSize)){
			@Override
			protected void onClick() {
				super.onClick();

				int ordinal = Tool.Size.valueOf(PixelDungeon.toolStyle()).ordinal();
				ordinal++;
				ordinal %= Tool.Size.values().length;
				Tool.Size size = Tool.Size.values()[ordinal];
				PixelDungeon.toolStyle(size.name());
			}
		});

		menuItems.setRect(0,0,WIDTH,menuItems.childsHeight());

		add(menuItems);

		resize(WIDTH, (int) menuItems.childsHeight());
	}

	private Selector createQuickSlotsSelector() {
		return new Selector(WIDTH, BTN_HEIGHT, Game
				.getVar(R.string.WndSettings_Quickslots), new Selector.PlusMinusDefault() {

			@Override
			public void onPlus(Selector s) {
				PixelDungeon.quickSlots(Math.min(PixelDungeon.quickSlots()+1, Toolbar.MAX_SLOTS));
			}

			@Override
			public void onMinus(Selector s) {
				PixelDungeon.quickSlots(Math.max(PixelDungeon.quickSlots()-1,0));
			}

			@Override
			public void onDefault(Selector s) {
				PixelDungeon.quickSlots(3);
			}
		});
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

	private Selector createUiZoomButtons() {
		return new Selector(WIDTH, BTN_HEIGHT, Game.getVar(R.string.WndSettings_UiScale), new Selector.PlusMinusDefault() {

			@Override
			public void onPlus(Selector s) {
				uiZoom(PixelScene.uiCamera.zoom + 0.1f, s);
			}

			@Override
			public void onMinus(Selector s) {
				uiZoom(PixelScene.uiCamera.zoom - 0.1f, s);
			}

			@Override
			public void onDefault(Selector s) {
				uiZoom(PixelScene.defaultZoom, s);
			}

			private void uiZoom(float value, Selector s) {
				PixelScene.uiCamera.updateFullscreenCameraZoom(value);
				((GameScene) Game.scene()).updateUiCamera();
				Preferences.INSTANCE.put(Preferences.KEY_UI_ZOOM, value);

				float zoom = PixelScene.uiCamera.zoom;
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
