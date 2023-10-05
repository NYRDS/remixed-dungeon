
package com.watabou.pixeldungeon.items.armor;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;


public class PlateArmor extends Armor {

	public PlateArmor() {
		super( 5 );
		image = 4;
	}
	
	@Override
	public String desc() {
        return StringsManager.getVar(R.string.PlateArmor_Desc);
    }
}
