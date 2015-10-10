package com.watabou.pixeldungeon.windows;

import java.io.IOException;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.SaveUtils;

public class WndSaveSlotSelect extends WndOptions {
	
	boolean saving;
	
	WndSaveSlotSelect(boolean _saving) {
		super(Game.getVar(R.string.WndSaveSlotSelect_SelectSlot), "", "1", "2", "3", "4", "5", "6", "7", "8", "9");
		saving = _saving;
	}
	
	@Override
	protected void onSelect( int index ) {
		String slot = Integer.toString(index+1);
		if(saving) {
			try {
				
				Dungeon.saveAll();
				SaveUtils.copySaveToSlot(slot, Dungeon.heroClass);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
		SaveUtils.loadGame(slot, Dungeon.hero.heroClass);
		
	};
	
}
