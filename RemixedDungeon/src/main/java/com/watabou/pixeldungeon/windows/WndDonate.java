package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.windows.WndHelper;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.game.RemixedDungeon;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Group;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.Icons;
import com.watabou.pixeldungeon.ui.SystemRedButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.windows.elements.RankingTab;
import com.watabou.pixeldungeon.windows.elements.Tab;

public class WndDonate extends WndTabbed {

	private static final Icons[] icons = { Icons.CHEST_SILVER,
			Icons.CHEST_GOLD, Icons.CHEST_RUBY, Icons.CHEST_ROYAL };

	public WndDonate() {
		EventCollector.logScene(getClass().getCanonicalName());

		resize(WndHelper.getFullscreenWidth(), WndHelper.getFullscreenHeight() - tabHeight() - 2*GAP);

        String[] labels = {
                StringsManager.getVar(R.string.WndDonate_silver),
                StringsManager.getVar(R.string.WndDonate_gold),
                StringsManager.getVar(R.string.WndDonate_ruby),
                StringsManager.getVar(R.string.WndDonate_royal)
		};
		Group[] pages = {	new DonateTab(1),
							new DonateTab(2),
							new DonateTab(3),
							new DonateTab(4) };

		for (int i = 0; i < pages.length; i++) {
			add(pages[i]);

			Tab tab = new RankingTab(this, labels[i], pages[i]);
			tab.setSize(width/pages.length, tabHeight());
			add(tab);
		}

		select(2);
	}

	@Override
	public void select(int index) {
		super.select(index);
		EventCollector.logScene(getClass().getCanonicalName()+":"+ index);
	}

	private class DonateTab extends Group {

		DonateTab(final int level) {

            final String[] title = {
                    StringsManager.getVar(R.string.WndDonate_silverDonate),
                    StringsManager.getVar(R.string.WndDonate_goldDonate),
                    StringsManager.getVar(R.string.WndDonate_rubyDonate),
                    StringsManager.getVar(R.string.WndDonate_royalDonate)
			};

			IconTitle tabTitle = new IconTitle(Icons.get(icons[level - 1]),
					title[level - 1]);
			tabTitle.setRect(0, 0, width, 0);
			add(tabTitle);

			float pos = tabTitle.bottom();

			pos += GAP;

			if (GamePreferences.donated() < level) {
				String price = RemixedDungeon.instance().iap.getDonationPriceString(level);
				String btnText;
				if( !price.isEmpty() ) {
                    btnText = StringsManager.getVar(R.string.WndDonate_donate) + " " + price;
				} else {
                    btnText = StringsManager.getVar(R.string.WndDonate_notConnected);
				}
				SystemRedButton donate = new SystemRedButton(btnText) {
					@Override
					protected void onClick() {
						EventCollector.logEvent("DonationClick",Integer.toString(level));
						RemixedDungeon.instance().iap.donate(level);
					}
				};
				
				if( price.isEmpty()) {
					donate.enable(false);
				}

				add(donate.setRect(0 ,height - BUTTON_HEIGHT, width, BUTTON_HEIGHT));
			}

            Text commonText = PixelScene.createMultiline(R.string.WndDonate_commonDonateText, GuiProperties.regularFontSize());
			commonText.maxWidth(width);
			commonText.setPos(0, pos);
			add(commonText);
			pos += commonText.height() + GAP;

            final int[] text = {
                    R.string.WndDonate_silverDonateText,
                    R.string.WndDonate_goldDonateText,
                    R.string.WndDonate_rubyDonateText,
                    R.string.WndDonate_royalDonateText
			};

			Text tabText = PixelScene.createMultiline(
					text[level - 1], GuiProperties.regularFontSize());
			tabText.maxWidth(width - 10);
			tabText.hardlight( Window.TITLE_COLOR );
			tabText.setPos(0, pos);
			add(tabText);
			
			pos += tabText.height() + GAP;

            final int[] text2 = {
                    R.string.WndDonate_silverDonateText2,
                    R.string.WndDonate_goldDonateText2,
                    R.string.WndDonate_rubyDonateText2,
                    R.string.WndDonate_royalDonateText2
			};

			Text tabText2 = PixelScene.createMultiline(
					text2[level - 1], GuiProperties.regularFontSize());
			tabText2.maxWidth(width - 10);
			tabText2.setPos(0, pos);
			add(tabText2);
		}
	}

}
