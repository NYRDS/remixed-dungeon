
package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.Selector;
import com.watabou.pixeldungeon.windows.WndMenuCommon;

public class WndUiSettings extends WndMenuCommon {

	@Override
	protected void createItems() {

		if (android.os.Build.VERSION.SDK_INT >= 19) {
			menuItems.add( new MenuCheckBox(Game.getVar(R.string.WndSettings_Immersive),PixelDungeon.immersed()) {
				@Override
				protected void onClick() {
					super.onClick();
					PixelDungeon.immerse(checked());
				}
			});
		}

		if(!PixelDungeon.classicFont()){
			menuItems.add(createTextScaleButtons());
		}

		menuItems.add(new MenuButton(orientationText()) {
			@Override
			protected void onClick() {
				PixelDungeon.landscape(!PixelDungeon.landscape());
			}
		});

		final String texts [] = {Game.getVar(R.string.WndSettings_ExperementalFont),
				Game.getVar(R.string.WndSettings_ClassicFont)
		};

		if (Utils.canUseClassicFont(PixelDungeon.uiLanguage())) {
			final int index = PixelDungeon.classicFont() ? 0 : 1;

			menuItems.add( new MenuButton(texts[index]) {
				@Override
				protected void onClick() {
					PixelDungeon.classicFont(!PixelDungeon.classicFont());
					WndUiSettings.this.getParent().add(new WndUiSettings());
					hide();
				}
			});
		}
	}

	private Selector createTextScaleButtons() {
		return new Selector(WIDTH,BTN_HEIGHT,Game
				.getVar(R.string.WndSettings_TextScaleDefault), new Selector.PlusMinusDefault() {
			@Override
			public void onPlus(Selector s) {
				PixelDungeon.fontScale(PixelDungeon.fontScale() + 1);
				s.regen();
			}

			@Override
			public void onMinus(Selector s) {
				PixelDungeon.fontScale(PixelDungeon.fontScale() - 1);
				s.regen();
			}

			@Override
			public void onDefault(Selector s) {
				PixelDungeon.fontScale(0);
				s.regen();
			}
		});
	}

	private String orientationText() {
		return PixelDungeon.landscape() ? Game
				.getVar(R.string.WndSettings_SwitchPort) : Game
				.getVar(R.string.WndSettings_SwitchLand);
	}
}