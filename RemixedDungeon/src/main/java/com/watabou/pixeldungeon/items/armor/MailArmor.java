
package com.watabou.pixeldungeon.items.armor;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;


public class MailArmor extends Armor {

	public MailArmor() {
		super( 3 );
		image = 2;
	}

	@Override
	public String desc() {
        return StringsManager.getVar(R.string.MailArmor_Desc);
    }
}
