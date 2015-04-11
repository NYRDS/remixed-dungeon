package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;

public class WndDonate extends Window {

	private static final String RUBY_DONATE   = Game.getVar(R.string.WndDonate_rubyDonate);
	private static final String GOLD_DONATE   = Game.getVar(R.string.WndDonate_goldDonate);
	private static final String SILVER_DONATE = Game.getVar(R.string.WndDonate_silverDonate);
	
	private static final int WIDTH      = 112;
	private static final int BTN_HEIGHT = 20;
	private static final int GAP        = 2;

	public WndDonate() {
		super();

		int donationLevel = PixelDungeon.donated();
		
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
	}
}
