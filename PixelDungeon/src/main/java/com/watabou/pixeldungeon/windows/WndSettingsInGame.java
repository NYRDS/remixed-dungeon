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
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.Preferences;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.CheckBox;

public class WndSettingsInGame extends WndSettingsCommon {

	public WndSettingsInGame() {
		super();

		curY = createZoomButtons(curY) + SMALL_GAP;
		curY = createUiZoomButtons(curY);

		CheckBox btnBrightness = new CheckBox(Game
				.getVar(R.string.WndSettings_Brightness)) {
			@Override
			protected void onClick() {
				super.onClick();
				PixelDungeon.brightness(checked());
			}
		};
		btnBrightness
				.setRect(0, curY + SMALL_GAP, WIDTH, BTN_HEIGHT);
		btnBrightness.checked(PixelDungeon.brightness());
		add(btnBrightness);

		final CheckBox secondQuickslot = new CheckBox(Game
				.getVar(R.string.WndSettings_SecondQuickslot)) {
			@Override
			protected void onClick() {
				super.onClick();
				PixelDungeon.secondQuickslot(checked());
			}
		};
		secondQuickslot.setRect(0, btnBrightness.bottom() + SMALL_GAP, WIDTH,
				BTN_HEIGHT);
		secondQuickslot.checked(PixelDungeon.secondQuickslot());

		add(secondQuickslot);

		if (PixelDungeon.landscape()) {
			CheckBox thirdQuickslot = new CheckBox(Game
					.getVar(R.string.WndSettings_ThirdQuickslot)) {
				@Override
				protected void onClick() {
					super.onClick();
					secondQuickslot.enable(!checked());
					PixelDungeon.thirdQuickslot(checked());
				}
			};

			secondQuickslot.enable(!PixelDungeon.thirdQuickslot());
			thirdQuickslot.setRect(0, secondQuickslot.bottom() + SMALL_GAP,
					WIDTH, BTN_HEIGHT);
			thirdQuickslot.checked(PixelDungeon.thirdQuickslot());
			add(thirdQuickslot);

			resize(WIDTH, (int) thirdQuickslot.bottom());
		} else {
			if (PixelDungeon.thirdQuickslot()) {
				PixelDungeon.secondQuickslot(true);
				secondQuickslot.checked(PixelDungeon.secondQuickslot());
			}
			resize(WIDTH, (int) secondQuickslot.bottom());
		}
	}

	private float createZoomButtons(float y) {
		final Selector selector = new Selector(this, WIDTH, BTN_HEIGHT);
		return selector.add(y, Game
				.getVar(R.string.WndSettings_ZoomDef), new Selector.PlusMinusDefault() {

			@Override
			public void onPlus() {
				zoom(Camera.main.zoom + 0.1f);
			}

			@Override
			public void onMinus() {
				zoom(Camera.main.zoom - 0.1f);
			}

			@Override
			public void onDefault() {
				zoom(PixelScene.defaultZoom);
			}

			private void zoom(float value) {
				Camera.main.zoom(value);
				PixelDungeon.zoom(value - PixelScene.defaultZoom);

				float zoom = Camera.main.zoom;
				selector.enable(zoom > PixelScene.minZoom, zoom < PixelScene.maxZoom, true);
			}
		});
	}

	private float createUiZoomButtons(float y) {
		final Selector selector = new Selector(this, WIDTH, BTN_HEIGHT);
		return selector.add(y, Game.getVar(R.string.WndSettings_UiScale), new Selector.PlusMinusDefault() {

			@Override
			public void onPlus() {
				uiZoom(PixelScene.uiCamera.zoom + 0.1f);
			}

			@Override
			public void onMinus() {
				uiZoom(PixelScene.uiCamera.zoom - 0.1f);
			}

			@Override
			public void onDefault() {
				uiZoom(PixelScene.defaultZoom);
			}

			private void uiZoom(float value) {
				PixelScene.uiCamera.updateFullscreenCameraZoom(value);
				((GameScene) Game.scene()).updateUiCamera();
				Preferences.INSTANCE.put(Preferences.KEY_UI_ZOOM, value);

				float zoom = PixelScene.uiCamera.zoom;
				selector.enable(zoom < PixelScene.maxZoom, zoom > PixelScene.minZoom, true);
			}
		});
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		PixelDungeon.resetScene();
	}
}
