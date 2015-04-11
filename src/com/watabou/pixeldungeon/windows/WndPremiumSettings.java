package com.watabou.pixeldungeon.windows;

import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;

public class WndPremiumSettings extends Window {

	private static final int WIDTH = 112;
	private static final int BTN_HEIGHT = 20;
	private static final int GAP = 2;

	private int curBottom = 0;

	public WndPremiumSettings() {
		super();

		createAssetsSelector("chrome", "chrome");
		createAssetsSelector("status", "status");
		createAssetsSelector("toolbar", "toolbar");
		
		resize(WIDTH, curBottom);
	}

	private void createAssetsSelector(final String assetKind, final String assetName) {
		RedButton btn = new RedButton(assetName) {
			@Override
			protected void onClick() {
				PixelDungeon.scene().add(
						new WndOptions(assetName +" type", "", "std", "silver",
								"gold", "ruby") {
							@Override
							protected void onSelect(int index) {
								if (PixelDungeon.donated() >= index) {
									Assets.use(assetKind, index);
									PixelDungeon.scene().add(
											new WndMessage("ok!"));
								} else {
									PixelDungeon.scene().add(
											new WndMessage("not avaliable"));
								}
							};
						});
			};
		};

		btn.setRect(0, curBottom, WIDTH, BTN_HEIGHT);
		add(btn);
		curBottom += BTN_HEIGHT + GAP;
	}
}
