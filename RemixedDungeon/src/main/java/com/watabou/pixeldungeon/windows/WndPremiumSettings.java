package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.RemixedDungeon;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.Utils;

import java.util.HashMap;
import java.util.Map;

public class WndPremiumSettings extends Window {

	private static Map<String, Integer> material2level = new HashMap<>();

	static {
		material2level.put(Game.getVar(R.string.WndPremiumSettings_std),0);
		material2level.put(Game.getVar(R.string.WndPremiumSettings_silver),1);
		material2level.put(Game.getVar(R.string.WndPremiumSettings_gold),2);
		material2level.put(Game.getVar(R.string.WndPremiumSettings_marble),2);
		material2level.put(Game.getVar(R.string.WndPremiumSettings_ruby),3);
		material2level.put(Game.getVar(R.string.WndPremiumSettings_royal),4);
	}

	private static final int WIDTH      = 112;

	private int curBottom = 0;

	public WndPremiumSettings() {
		super();
		
		createAssetsSelector("chrome",
				Game.getVar(R.string.WndPremiumSettings_chrome),
				Game.getVar(R.string.WndPremiumSettings_std),
				Game.getVar(R.string.WndPremiumSettings_silver),
				Game.getVar(R.string.WndPremiumSettings_gold),
				Game.getVar(R.string.WndPremiumSettings_ruby),
				Game.getVar(R.string.WndPremiumSettings_marble),
				Game.getVar(R.string.WndPremiumSettings_royal)
		);
		createAssetsSelector("status",
				Game.getVar(R.string.WndPremiumSettings_status),
				Game.getVar(R.string.WndPremiumSettings_std),
				Game.getVar(R.string.WndPremiumSettings_silver),
				Game.getVar(R.string.WndPremiumSettings_gold),
				Game.getVar(R.string.WndPremiumSettings_ruby),
				Game.getVar(R.string.WndPremiumSettings_royal)
		);
		createAssetsSelector("banners",
				Game.getVar(R.string.WndPremiumSettings_banners),
				Game.getVar(R.string.WndPremiumSettings_std),
				Game.getVar(R.string.WndPremiumSettings_silver),
				Game.getVar(R.string.WndPremiumSettings_gold),
				Game.getVar(R.string.WndPremiumSettings_ruby),
				Game.getVar(R.string.WndPremiumSettings_royal)
		);
		
		resize(WIDTH, curBottom);
	}

	private void createAssetsSelector(final String assetKind, final String assetName, final String... options  ) {
		RedButton btn = new RedButton(assetName) {
			@Override
			protected void onClick() {
				RemixedDungeon.scene().add(
						new WndOptions(assetName, Utils.EMPTY_STRING, options) {
							@Override
							public void onSelect(int index) {
								if (RemixedDungeon.donated() >= material2level.get(options[index])) {
									Assets.use(assetKind, index);
									RemixedDungeon.scene().add(
											new WndMessage("ok!"));
								} else {
									RemixedDungeon.scene().add(
											new WndMessage(Game.getVar(R.string.WndPremiumSettings_notAvailbale)));
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
		RemixedDungeon.resetScene();
	}
}
