
package com.watabou.pixeldungeon.items.armor;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;


public class ScaleArmor extends Armor {

	public ScaleArmor() {
		super( 4 );
		image = 3;
	}
	
	@Override
	public String desc() {
        return StringsManager.getVar(R.string.ScaleArmor_Desc);
    }
}
