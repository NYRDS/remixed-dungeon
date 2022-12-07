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
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.ui.CheckBox;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.utils.Utils;

public class WndSettings extends WndSettingsCommon {

	private Selector fontScaleSelector = new Selector(this, WIDTH, BTN_HEIGHT);

	private RedButton btnFontMode;

	public WndSettings() {
		super();

		if (android.os.Build.VERSION.SDK_INT >= 19) {
			CheckBox btnImmersive = new CheckBox(Game.getVar(R.string.WndSettings_Immersive)) {
				@Override
				protected void onClick() {
					super.onClick();
					PixelDungeon.immerse(checked());
				}
			};

			btnImmersive.setRect(0, curY, WIDTH,
					BTN_HEIGHT);
			btnImmersive.checked(PixelDungeon.immersed());
			add(btnImmersive);
			curY = btnImmersive.bottom() + SMALL_GAP;
		}

		curY = createTextScaleButtons(curY);

		RedButton btnOrientation = new RedButton(orientationText()) {
			@Override
			protected void onClick() {
				PixelDungeon.landscape(!PixelDungeon.landscape());
			}
		};
		btnOrientation.setRect(0, curY + SMALL_GAP, WIDTH,
				BTN_HEIGHT);
		add(btnOrientation);

		CheckBox btnRealtime = new CheckBox("Realtime!") {
			@Override
			protected void onClick() {
				super.onClick();
				PixelDungeon.realtime(checked());
			}
		};
		btnRealtime.setRect(0, btnOrientation.bottom() + SMALL_GAP, WIDTH,
				BTN_HEIGHT);
		btnRealtime.checked(PixelDungeon.realtime());
		if (!PixelDungeon.isAlpha()) {
			btnRealtime.enable(false);
		}
		add(btnRealtime);

		final String selectLanguage = Game.getVar(R.string.WndSettings_SelectLanguage);

		RedButton localeButton = new RedButton(selectLanguage) {
			@Override
			protected void onClick() {
				PixelDungeon.scene().add(
						new WndSelectLanguage(selectLanguage, "English",
								"Русский", "Français", "Polski", "Español", "한국말", "Português brasileiro", "Italiano", "Deutsch", "简体中文", "日本語", "Türkçe", "Украї́нська") {

							@Override
							protected void onSelect(int index) {
								String lang[] = {"en", "ru", "fr", "pl", "es", "ko", "pt_BR", "it", "de", "zh", "ja", "tr", "uk"};
								if (!Utils.canUseClassicFont(lang[index])) {
									PixelDungeon.classicFont(false);
								}
								PixelDungeon.uiLanguage(lang[index]);
							}
						});
			}
		};

		localeButton.setRect(0, btnRealtime.bottom() + SMALL_GAP, WIDTH,
				BTN_HEIGHT);
		add(localeButton);

		float y = createFontSelector(localeButton.bottom() + SMALL_GAP);
		y = createMoveTimeoutSelector(y + SMALL_GAP);

		resize(WIDTH, (int) y);
	}

	private float createFontSelector(float y) {
		remove(btnFontMode);

		String text;

		if (PixelDungeon.classicFont()) {
			text = Game
					.getVar(R.string.WndSettings_ExperementalFont);
			fontScaleSelector.enable(false);
		} else {
			text = Game
					.getVar(R.string.WndSettings_ClassicFont);
			fontScaleSelector.enable(true);
		}

		btnFontMode = new RedButton(text) {
			@Override
			protected void onClick() {
				PixelDungeon.classicFont(!PixelDungeon.classicFont());
				createFontSelector(y);
			}
		};

		if (!Utils.canUseClassicFont(PixelDungeon.uiLanguage())) {
			btnFontMode.enable(false);
		}

		btnFontMode.setRect(0, y, WIDTH,
				BTN_HEIGHT);
		add(btnFontMode);

		return btnFontMode.bottom();
	}

	private float createTextScaleButtons(final float y) {
		return fontScaleSelector.add(y, Game
				.getVar(R.string.WndSettings_TextScaleDefault), new Selector.PlusMinusDefault() {
			@Override
			public void onPlus() {
				fontScaleSelector.remove();
				PixelDungeon.fontScale(PixelDungeon.fontScale() + 1);
				createTextScaleButtons(y);

			}

			@Override
			public void onMinus() {
				fontScaleSelector.remove();
				PixelDungeon.fontScale(PixelDungeon.fontScale() - 1);
				createTextScaleButtons(y);
			}

			@Override
			public void onDefault() {
				fontScaleSelector.remove();
				PixelDungeon.fontScale(0);
				createTextScaleButtons(y);
			}
		});
	}

	private String orientationText() {
		return PixelDungeon.landscape() ? Game
				.getVar(R.string.WndSettings_SwitchPort) : Game
				.getVar(R.string.WndSettings_SwitchLand);
	}

	private String moveTimeoutText() {
		return String.format(Game.getVar(R.string.WndSettings_moveTimeout),Double.toString(PixelDungeon.getMoveTimeout()/1000));
	}

	private float createMoveTimeoutSelector(final float y) {

		final Selector timeoutSelector = new Selector(this, WIDTH, BTN_HEIGHT);

		return timeoutSelector.add(y, moveTimeoutText(), new Selector.PlusMinusDefault() {

			private int selectedTimeout = PixelDungeon.limitTimeoutIndex(PixelDungeon.moveTimeout());

			private void update() {
				PixelDungeon.moveTimeout(selectedTimeout);
				timeoutSelector.setText(moveTimeoutText());
			}

			@Override
			public void onPlus() {
				selectedTimeout = PixelDungeon.limitTimeoutIndex(selectedTimeout+1);

				update();
			}

			@Override
			public void onMinus() {
				selectedTimeout = PixelDungeon.limitTimeoutIndex(selectedTimeout-1);

				update();
			}

			@Override
			public void onDefault() {
			}
		});
	}
}