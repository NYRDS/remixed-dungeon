
package com.watabou.pixeldungeon.items.rings;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;

public class RingOfHerbalism extends Ring {
	
	@Override
	public  ArtifactBuff buff( ) {
		return new Herbalism();
	}
	
	@Override
	public String desc() {
        return isKnown() ? StringsManager.getVar(R.string.RingOfHerbalism_Info) : super.desc();
	}
	
	public class Herbalism extends RingBuff {
	}
}
