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
import com.nyrds.pixeldungeon.windows.WndGameplaySettings;
import com.nyrds.pixeldungeon.windows.WndUiSettings;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.RemixedDungeon;
import com.watabou.pixeldungeon.utils.Utils;

public class WndSettings extends WndMenuCommon {

	@Override
	protected void createItems() {
		addSoundControls(menuItems);

		menuItems.add(new MenuButton(Game.getVar(R.string.WndSettings_UiSettings)){
			@Override
			protected void onClick() {
				super.onClick();
				WndSettings.this.add(new WndUiSettings());
			}
		});

		menuItems.add(new MenuButton(Game.getVar(R.string.WndSettings_GameplaySettings)){
			@Override
			protected void onClick() {
				super.onClick();
				WndSettings.this.add(new WndGameplaySettings());
			}
		});


		final String selectLanguage = Game.getVar(R.string.WndSettings_SelectLanguage);

		menuItems.add(new MenuButton(selectLanguage) {
			@Override
			protected void onClick() {
				RemixedDungeon.scene().add(
						new WndSelectLanguage(selectLanguage, "English",
								"Русский", "Français", "Polski", "Español", "한국말", "Português brasileiro", "Italiano", "Deutsch", "简体中文", /*"日本語",*/ "Türkçe", "Украї́нська","Bahasa Melayu","Magyar Nyelv") {

							@Override
							protected void onSelect(int index) {
								String[] lang = {"en", "ru", "fr", "pl", "es", "ko", "pt_BR", "it", "de", "zh", /*"ja",*/ "tr", "uk","ms","hu"};
								if (!Utils.canUseClassicFont(lang[index])) {
									RemixedDungeon.classicFont(false);
								}
								RemixedDungeon.uiLanguage(lang[index]);
							}
						});
			}
		});
	}
}