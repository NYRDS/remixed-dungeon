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
import com.nyrds.pixeldungeon.windows.WndUiSettings;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.utils.Utils;

public class WndSettings extends WndSettingsCommon {

	public WndSettings() {
		super();

		VBox menuItems = new VBox();


		menuItems.add(new MenuButton(Game.getVar(R.string.WndSettings_UiSettings)){
			@Override
			protected void onClick() {
				super.onClick();
				WndSettings.this.add(new WndUiSettings());
			}
		});

		addSoundControls(menuItems);

		if (!PixelDungeon.isAlpha()) {
			menuItems.add(new MenuCheckBox("Realtime!",PixelDungeon.realtime()) {
				@Override
				protected void onClick() {
					super.onClick();
					PixelDungeon.realtime(checked());
				}
			});
		}

		final String selectLanguage = Game.getVar(R.string.WndSettings_SelectLanguage);

		menuItems.add(new MenuButton(selectLanguage) {
			@Override
			protected void onClick() {
				PixelDungeon.scene().add(
						new WndSelectLanguage(selectLanguage, "English",
								"Русский", "Français", "Polski", "Español", "한국말", "Português brasileiro", "Italiano", "Deutsch", "简体中文", "日本語", "Türkçe", "Украї́нська","Bahasa Melayu") {

							@Override
							protected void onSelect(int index) {
								String lang[] = {"en", "ru", "fr", "pl", "es", "ko", "pt_BR", "it", "de", "zh", "ja", "tr", "uk","ms"};
								if (!Utils.canUseClassicFont(lang[index])) {
									PixelDungeon.classicFont(false);
								}
								PixelDungeon.uiLanguage(lang[index]);
							}
						});
			}
		});

		menuItems.add(createMoveTimeoutSelector());

		menuItems.setRect(0,0,width,menuItems.childsHeight());
		add(menuItems);

		resize(WIDTH, (int) menuItems.childsHeight());
	}

	private String moveTimeoutText() {
		return String.format(Game.getVar(R.string.WndSettings_moveTimeout),Double.toString(PixelDungeon.getMoveTimeout()/1000));
	}

	private Selector createMoveTimeoutSelector() {

		return new Selector( WIDTH, BTN_HEIGHT, moveTimeoutText(), new Selector.PlusMinusDefault() {

			private int selectedTimeout = PixelDungeon.limitTimeoutIndex(PixelDungeon.moveTimeout());

			private void update(Selector s) {
				PixelDungeon.moveTimeout(selectedTimeout);
				s.setText(moveTimeoutText());
			}

			@Override
			public void onPlus(Selector s) {
				selectedTimeout = PixelDungeon.limitTimeoutIndex(selectedTimeout+1);
				update(s);
			}

			@Override
			public void onMinus(Selector s) {
				selectedTimeout = PixelDungeon.limitTimeoutIndex(selectedTimeout-1);
				update(s);
			}

			@Override
			public void onDefault(Selector s) {
			}
		});
	}
}