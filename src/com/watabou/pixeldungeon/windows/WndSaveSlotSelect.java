package com.watabou.pixeldungeon.windows;

import java.io.IOException;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.GameWithGoogleIap;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.SaveUtils;
import com.watabou.pixeldungeon.ui.DonateButton;

public class WndSaveSlotSelect extends WndOptionsColumns implements GameWithGoogleIap.IntersitialPoint{
	
	private boolean saving;
	private String slot;
	
	WndSaveSlotSelect(boolean _saving) {
		super(Game.getVar(R.string.WndSaveSlotSelect_SelectSlot), windowText(), "1", "2", "3", "4", "5", "6", "7", "8", "9");
		saving = _saving;
		
		if(!saving) {
			for (int i = 1; i<10; i++) {
				if(!SaveUtils.slotUsed(Integer.toString(i), Dungeon.heroClass)) {
					setEnabled(i-1, false);
				}
			}
		}
		
		if(PixelDungeon.donated() == 0 && PixelDungeon.canDonate()) {
			DonateButton btn = new DonateButton();
			add(btn);
			btn.setPos(width/2 - btn.width()/2, height);
			resize(width,(int) (height+btn.height()));
		}
	}
	
	private static String windowText() {
		if(PixelDungeon.donated() == 0 && PixelDungeon.canDonate()) {
			return Game.getVar(R.string.WndSaveSlotSelect_dontLike);
		}
		return "";
	}
	
	@Override
	protected void onSelect( int index ) {
		slot = Integer.toString(index+1);
		
		final GameWithGoogleIap.IntersitialPoint returnTo = this;
		
		if(saving) {
			try {
				
				Dungeon.saveAll();
				SaveUtils.copySaveToSlot(slot, Dungeon.heroClass);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		Game.paused = true;
		
		if(PixelDungeon.donated() < 1) {
			Game.instance().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					PixelDungeon.displayAd(returnTo);
				}
			});
		} else {
			returnToWork();
		}
	}

	@Override
	public void returnToWork() {
		Game.paused = false;
		if(!saving) {
			SaveUtils.loadGame(slot, Dungeon.hero.heroClass);
		}
	}

}
