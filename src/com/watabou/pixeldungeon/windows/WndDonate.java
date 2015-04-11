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


public class WndDonate extends WndTabbed  {

	private static final String RUBY   = Game.getVar(R.string.WndDonate_ruby);
	private static final String GOLD   = Game.getVar(R.string.WndDonate_gold);
	private static final String SILVER = Game.getVar(R.string.WndDonate_silver);
	
	private static final String RUBY_DONATE   = Game.getVar(R.string.WndDonate_rubyDonate);
	private static final String GOLD_DONATE   = Game.getVar(R.string.WndDonate_goldDonate);
	private static final String SILVER_DONATE = Game.getVar(R.string.WndDonate_silverDonate);
	
	private static final String SILVER_DONATE_TEXT = Game.getVar(R.string.WndDonate_silverDonateText);
	private static final String GOLD_DONATE_TEXT   = Game.getVar(R.string.WndDonate_goldDonateText);
	private static final String RUBY_DONATE_TEXT   = Game.getVar(R.string.WndDonate_rubyDonateText);
	
	private static final int WIDTH      = 112;
	private static final int BTN_HEIGHT = 20;
	private static final int GAP        = 2;
	private static final int TAB_WIDTH  = 35;

	public void  createTabs() {

		String[] labels = 
			{SILVER, GOLD, RUBY};
		Group[] pages = 
			{new SilverTab(), new SilverTab(), new SilverTab()};
		
		for (int i=0; i < pages.length; i++) {
			
			add( pages[i] );
			
			Tab tab = new RankingTab(this, labels[i], pages[i] );
			tab.setSize( TAB_WIDTH , tabHeight() );
			add( tab );
		}
	}
	
	public WndDonate() {
		super();

		int donationLevel = PixelDungeon.donated();
		/*
		RedButton silverDonate = new RedButton(SILVER_DONATE) {
			@Override
			protected void onClick() {
				//PixelDungeon.donate(1);
			}
		};
		add(silverDonate.setRect(0, 0, WIDTH, BTN_HEIGHT));
		
		if(donationLevel > 0){
			silverDonate.enable(false);
		}
		
		RedButton goldDonate = new RedButton(GOLD_DONATE) {
			@Override
			protected void onClick() {
				//PixelDungeon.donate(2);
			}
		};
		add(goldDonate.setRect(0, silverDonate.bottom() + GAP, WIDTH,
				BTN_HEIGHT));

		if(donationLevel > 1){
			silverDonate.enable(false);
		}
		
		RedButton rubyDonate = new RedButton(RUBY_DONATE) {
			@Override
			protected void onClick() {
				//PixelDungeon.donate(3);
			}
		};
		add(rubyDonate.setRect(0, goldDonate.bottom() + GAP, WIDTH, BTN_HEIGHT));

		if(donationLevel > 2){
			goldDonate.enable(false);
		}
		
		resize(WIDTH, (int) rubyDonate.bottom());
		*/
		createTabs();
		resize(WIDTH, WIDTH);
	}
	
	private class SilverTab extends Group {
		
		private static final int GAP	= 4;
		
		public SilverTab() {
			super();
			
			IconTitle title = new IconTitle(Icons.get(Icons.CHEST_SILVER), SILVER_DONATE);
			title.setRect( 0, 0, WIDTH, 0 );
			add( title );
			
			float pos = title.bottom();
			
			pos += GAP;
			
			BitmapTextMultiline text = PixelScene.createMultiline( SILVER_DONATE_TEXT, 7 );
			text.measure();
			text.setPos(0,pos);
			add(text);
			
			pos += text.height() + GAP;
			
			RedButton silverDonate = new RedButton(SILVER_DONATE) {
				@Override
				protected void onClick() {
					//PixelDungeon.donate(1);
				}
			};
			add(silverDonate.setRect(0, pos, WIDTH, BTN_HEIGHT));
			
			if(PixelDungeon.donated() > 0){
				silverDonate.enable(false);
			}
		}

	}

}
