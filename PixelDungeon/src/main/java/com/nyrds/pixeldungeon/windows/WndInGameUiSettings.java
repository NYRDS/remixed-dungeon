
package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.Preferences;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.Toolbar;
import com.watabou.pixeldungeon.windows.Selector;
import com.watabou.pixeldungeon.windows.WndMenuCommon;
import com.watabou.pixeldungeon.windows.elements.Tool;

public class WndInGameUiSettings extends WndMenuCommon {

	@Override
	protected void createItems() {

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

		menuItems.add(new MenuButton(Game.getVar(R.string.WndSettings_Handedness)){
			@Override
			protected void onClick() {
				super.onClick();
				PixelDungeon.handedness(!PixelDungeon.handedness());
			}
		});
	}

	private Selector createQuickSlotsSelector() {
		return new Selector(WIDTH, BTN_HEIGHT, Game
				.getVar(R.string.WndSettings_Quickslots), new Selector.PlusMinusDefault() {

			@Override
			public void onPlus(Selector s) {
				PixelDungeon.quickSlots(Math.min(Math.abs(PixelDungeon.quickSlots())+1, Toolbar.MAX_SLOTS));
			}

			@Override
			public void onMinus(Selector s) {

				PixelDungeon.quickSlots(Math.max(Math.abs(PixelDungeon.quickSlots())-1,0));
			}

			@Override
			public void onDefault(Selector s) {
				PixelDungeon.quickSlots(-1);
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
}