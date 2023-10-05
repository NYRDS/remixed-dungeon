
package com.watabou.pixeldungeon.items.armor;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;


public class LeatherArmor extends Armor {

	public LeatherArmor() {
		super( 2 );
		image = 1;
	}
	
	@Override
	public String desc() {
        return StringsManager.getVar(R.string.LeatherArmor_Desc);
    }
}
