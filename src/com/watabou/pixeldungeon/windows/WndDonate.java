package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.BitmapTextMultiline;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.Icons;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.windows.elements.RankingTab;
import com.watabou.pixeldungeon.windows.elements.Tab;

public class WndDonate extends WndTabbed {

	private static final String RUBY   = Game.getVar(R.string.WndDonate_ruby);
	private static final String GOLD   = Game.getVar(R.string.WndDonate_gold);
	private static final String SILVER = Game.getVar(R.string.WndDonate_silver);

	private static final String DONATE = Game.getVar(R.string.WndDonate_donate);

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

	private static final int WIDTH = 111;
	private static final int HEIGHT = 111;

	private static final int BTN_HEIGHT = 20;
	private static final int BTN_WIDTH  = 80;
	
	private static final int GAP = 2;
	private static final int TAB_WIDTH = 41;

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

		resize(HEIGHT, WIDTH);

		select(0);
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
				RedButton donate = new RedButton(DONATE + PixelDungeon.instance().getPriceString(level)) {
					@Override
					protected void onClick() {
						PixelDungeon.donate(level);
					}
				};
				add(donate.setRect(WIDTH - BTN_WIDTH ,HEIGHT - BTN_HEIGHT, BTN_WIDTH, BTN_HEIGHT));
			}

			BitmapTextMultiline tabText = PixelScene.createMultiline(
					text[level - 1], 7);
			tabText.measure();
			tabText.setPos(0, pos);
			add(tabText);
		}
	}

}
