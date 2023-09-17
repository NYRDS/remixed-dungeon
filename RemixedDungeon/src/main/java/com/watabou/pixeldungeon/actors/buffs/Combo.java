
package com.watabou.pixeldungeon.actors.buffs;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.ui.BuffIndicator;
import com.watabou.pixeldungeon.utils.GLog;

public class Combo extends Buff {
	
	public int count = 0;
	
	@Override
	public int icon() {
		return BuffIndicator.COMBO;
	}

	public int hit( Char enemy, int damage ) {
		
		count++;
		
		if (count >= 3) {
			
			Badges.validateMasteryCombo( count );

            GLog.p(StringsManager.getVar(R.string.Combo_Combo), count );
			postpone( 1.41f - count / 10f );
			return (int)(damage * (count - 2) / 5f);
			
		} else {
			
			postpone( 1.1f );
			return 0;
			
		}
	}
	
	@Override
	public boolean act() {
		detach();
		return true;
	}
	
}
