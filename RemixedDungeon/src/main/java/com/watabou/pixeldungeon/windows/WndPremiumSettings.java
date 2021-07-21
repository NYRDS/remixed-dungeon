package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.Utils;

import java.util.HashMap;
import java.util.Map;

public class WndPremiumSettings extends Window {

	private static final Map<String, Integer> material2level = new HashMap<>();

	static {
        material2level.put(StringsManager.getVar(R.string.WndPremiumSettings_std),0);
        material2level.put(StringsManager.getVar(R.string.WndPremiumSettings_silver),1);
        material2level.put(StringsManager.getVar(R.string.WndPremiumSettings_gold),2);
        material2level.put(StringsManager.getVar(R.string.WndPremiumSettings_marble),2);
        material2level.put(StringsManager.getVar(R.string.WndPremiumSettings_ruby),3);
        material2level.put(StringsManager.getVar(R.string.WndPremiumSettings_royal),4);
	}

	private static final int WIDTH      = 112;

	private int curBottom = 0;

	public WndPremiumSettings() {
		super();

        createAssetsSelector("chrome",
                StringsManager.getVar(R.string.WndPremiumSettings_chrome),
                StringsManager.getVar(R.string.WndPremiumSettings_std),
                StringsManager.getVar(R.string.WndPremiumSettings_silver),
                StringsManager.getVar(R.string.WndPremiumSettings_gold),
                StringsManager.getVar(R.string.WndPremiumSettings_ruby),
                StringsManager.getVar(R.string.WndPremiumSettings_marble),
                StringsManager.getVar(R.string.WndPremiumSettings_royal)
		);
        createAssetsSelector("status",
                StringsManager.getVar(R.string.WndPremiumSettings_status),
                StringsManager.getVar(R.string.WndPremiumSettings_std),
                StringsManager.getVar(R.string.WndPremiumSettings_silver),
                StringsManager.getVar(R.string.WndPremiumSettings_gold),
                StringsManager.getVar(R.string.WndPremiumSettings_ruby),
                StringsManager.getVar(R.string.WndPremiumSettings_royal)
		);
        createAssetsSelector("banners",
                StringsManager.getVar(R.string.WndPremiumSettings_banners),
                StringsManager.getVar(R.string.WndPremiumSettings_std),
                StringsManager.getVar(R.string.WndPremiumSettings_silver),
                StringsManager.getVar(R.string.WndPremiumSettings_gold),
                StringsManager.getVar(R.string.WndPremiumSettings_ruby),
                StringsManager.getVar(R.string.WndPremiumSettings_royal)
		);
		
		resize(WIDTH, curBottom);
	}

	private void createAssetsSelector(final String assetKind, final String assetName, final String... options  ) {
		RedButton btn = new RedButton(assetName) {
			@Override
			protected void onClick() {
				GameLoop.scene().add(
						new WndOptions(assetName, Utils.EMPTY_STRING, options) {
							@Override
							public void onSelect(int index) {
								if (GamePreferences.donated() >= material2level.get(options[index])) {
									Assets.use(assetKind, index);
									GameLoop.scene().add(
											new WndMessage("ok!"));
								} else {
                                    GameLoop.scene().add(
											new WndMessage(StringsManager.getVar(R.string.WndPremiumSettings_notAvailbale)));
								}
							}
						});
			}
		};

		btn.setRect(0, curBottom, WIDTH, BUTTON_HEIGHT);
		add(btn);
		curBottom += BUTTON_HEIGHT;
	}
	
	@Override
	public void onBackPressed() {
		hide();
		GameLoop.resetScene();
	}
}
