
package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.pixeldungeon.ml.BuildConfig;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.windows.WndGameplaySettings;
import com.nyrds.pixeldungeon.windows.WndUiSettings;
import com.nyrds.pixeldungeon.windows.WndVkPlayLink;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.utils.Utils;

public class WndSettings extends WndMenuCommon {

	static public final String[] langNames = {"English",
			"Русский", "Français", "Polski", "Español", "한국말", "Português brasileiro", "Italiano", "Deutsch", "简体中文","繁體中文", "Türkçe", "Украї́нська","Bahasa Melayu","Magyar Nyelv","Bahasa Indonesia","Ελληνικά","日本語"};
	static public final String[] lang = {"en", "ru", "fr", "pl", "es", "ko", "pt_BR", "it", "de", "zh_CN", "zh_TW", "tr", "uk","ms","hu","in","el","ja"};

	@Override
	protected void createItems() {
		addSoundControls(menuItems);

        menuItems.add(new MenuButton(R.string.WndSettings_UiSettings){
			@Override
			protected void onClick() {
				super.onClick();
				WndSettings.this.add(new WndUiSettings());
			}
		});

        menuItems.add(new MenuButton(R.string.WndSettings_GameplaySettings){
			@Override
			protected void onClick() {
				super.onClick();
				WndSettings.this.add(new WndGameplaySettings());
			}
		});


        boolean adsFlavor = Utils.isAndroid()
                && (BuildConfig.FLAVOR_market.equals("googlePlay") || BuildConfig.FLAVOR_market.equals("ruStore"));
        if (adsFlavor && GamePreferences.donated() == 0) {
            menuItems.add(new MenuButton(R.string.VkPlayLink_row) {
                @Override
                protected void onClick() {
                    super.onClick();
                    WndSettings.this.add(new WndVkPlayLink());
                }
            });
        }

        final String selectLanguage = StringsManager.getVar(R.string.WndSettings_SelectLanguage);

		menuItems.add(new MenuButton(selectLanguage) {
			@Override
			protected void onClick() {
				GameLoop.scene().add(
						new WndSelectLanguage(selectLanguage, langNames) {

							@Override
							protected void onSelect(int index) {
								GamePreferences.uiLanguage(lang[index]);
							}
						});
			}
		});
	}
}