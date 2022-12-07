package com.watabou.pixeldungeon.windows;

import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;

public class WndPremiumSettings extends Window {

	private static final String NOT_AVALIABLE =  Game.getVar(R.string.WndPremiumSettings_notAvailbale);
	
	private static final String TOOLBAR = Game.getVar(R.string.WndPremiumSettings_toolbar);
	private static final String STATUS  = Game.getVar(R.string.WndPremiumSettings_status);
	private static final String CHROME  = Game.getVar(R.string.WndPremiumSettings_chrome);
	private static final String BANNERS = Game.getVar(R.string.WndPremiumSettings_banners);
	
	private static final String RUBY   = Game.getVar(R.string.WndPremiumSettings_ruby);
	private static final String GOLD   = Game.getVar(R.string.WndPremiumSettings_gold);
	private static final String SILVER = Game.getVar(R.string.WndPremiumSettings_silver);
	private static final String STD    = Game.getVar(R.string.WndPremiumSettings_std);
		
	private static final int WIDTH      = 112;

	private int curBottom = 0;

	public WndPremiumSettings() {
		super();
		
		createAssetsSelector("chrome",  CHROME);
		createAssetsSelector("status",  STATUS);
		createAssetsSelector("toolbar", TOOLBAR);
		createAssetsSelector("banners", BANNERS);
		
		resize(WIDTH, curBottom);
	}

	private void createAssetsSelector(final String assetKind, final String assetName) {
		RedButton btn = new RedButton(assetName) {
			@Override
			protected void onClick() {
				PixelDungeon.scene().add(
						new WndOptions(assetName, "", STD, SILVER,
								GOLD, RUBY) {
							@Override
							protected void onSelect(int index) {
								if (PixelDungeon.donated() >= index) {
									Assets.use(assetKind, index);
									PixelDungeon.scene().add(
											new WndMessage("ok!"));
								} else {
									PixelDungeon.scene().add(
											new WndMessage(NOT_AVALIABLE));
								}
							}
						});
			}
		};

		btn.setRect(0, curBottom, WIDTH, BUTTON_HEIGHT);
		add(btn);
		curBottom += BUTTON_HEIGHT + GAP;
	}
	
	@Override
	public void onBackPressed() {
		hide();
		PixelDungeon.resetScene();
	}
}
