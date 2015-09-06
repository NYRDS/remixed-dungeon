package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.Icons;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.windows.elements.RankingTab;
import com.watabou.pixeldungeon.windows.elements.Tab;

public class WndDonate extends WndTabbed {

	private static final String RUBY   = Game.getVar(R.string.WndDonate_ruby);
	private static final String GOLD   = Game.getVar(R.string.WndDonate_gold);
	private static final String SILVER = Game.getVar(R.string.WndDonate_silver);

	private static final String DONATE = Game.getVar(R.string.WndDonate_donate);
	private static final String NOT_CONNECTED =  Game.getVar(R.string.WndDonate_notConnected);

	private static final String RUBY_DONATE = Game
			.getVar(R.string.WndDonate_rubyDonate);
	private static final String GOLD_DONATE = Game
			.getVar(R.string.WndDonate_goldDonate);
	private static final String SILVER_DONATE = Game
			.getVar(R.string.WndDonate_silverDonate);

	private static final String SILVER_DONATE_TEXT = Game
			.getVar(R.string.WndDonate_silverDonateText);
	private static final String GOLD_DONATE_TEXT = Game
			.getVar(R.string.WndDonate_goldDonateText);
	private static final String RUBY_DONATE_TEXT = Game
			.getVar(R.string.WndDonate_rubyDonateText);

	private static final int WIDTH = 130;
	private static final int HEIGHT = 145;

	private static final int BTN_HEIGHT = 20;
	private static final int BTN_WIDTH  = WIDTH;
	
	private static final int GAP = 2;
	private static final int TAB_WIDTH = 47;

	private static final Icons[] icons = { Icons.CHEST_SILVER,
			Icons.CHEST_GOLD, Icons.CHEST_RUBY };
	private static final String[] title = { SILVER_DONATE, GOLD_DONATE,
			RUBY_DONATE };
	private static final String[] text = { SILVER_DONATE_TEXT,
			GOLD_DONATE_TEXT, RUBY_DONATE_TEXT };

	public WndDonate() {
		super();
		String[] labels = { SILVER, GOLD, RUBY };
		Group[] pages = { new DonateTab(1), new DonateTab(2), new DonateTab(3) };

		for (int i = 0; i < pages.length; i++) {
			add(pages[i]);

			Tab tab = new RankingTab(this, labels[i], pages[i]);
			tab.setSize(TAB_WIDTH, tabHeight());
			add(tab);
		}

		resize(WIDTH,HEIGHT);

		select(1);
	}

	private class DonateTab extends Group {

		public DonateTab(final int level) {
			super();

			IconTitle tabTitle = new IconTitle(Icons.get(icons[level - 1]),
					title[level - 1]);
			tabTitle.setRect(0, 0, WIDTH, 0);
			add(tabTitle);

			float pos = tabTitle.bottom();

			pos += GAP;

			if (PixelDungeon.donated() < level) {
				String price = PixelDungeon.instance().getPriceString(level);
				String btnText;
				if( price != null ) {
					btnText = DONATE + price;
				} else {
					btnText = NOT_CONNECTED;
				}
				RedButton donate = new RedButton(btnText) {
					@Override
					protected void onClick() {
						PixelDungeon.donate(level);
					}
				};
				
				if( price == null) {
					donate.enable(false);
				}
				
				add(donate.setRect(WIDTH - BTN_WIDTH ,HEIGHT - BTN_HEIGHT, BTN_WIDTH, BTN_HEIGHT));
			}

			Text commonText = PixelScene.createMultiline(
					Game.getVar(R.string.WndDonate_commonDonateText), 6);
			commonText.maxWidth(WIDTH);
			commonText.measure();
			commonText.setPos(0, pos);
			add(commonText);
			pos += commonText.height() + GAP;
			
			Text tabText = PixelScene.createMultiline(
					text[level - 1], 7);
			tabText.maxWidth(WIDTH - 10);
			tabText.hardlight( Window.TITLE_COLOR );
			tabText.measure();
			tabText.setPos(0, pos);
			add(tabText);
		}
	}

}
