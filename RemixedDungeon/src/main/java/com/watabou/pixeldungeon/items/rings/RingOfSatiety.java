
package com.watabou.pixeldungeon.items.rings;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;

public class RingOfSatiety extends Ring {
	
	@Override
	public ArtifactBuff buff( ) {
		return new Satiety();
	}
	
	@Override
	public String desc() {
        return isKnown() ? StringsManager.getVar(R.string.RingOfSatiety_Info) : super.desc();
	}
	
	public class Satiety extends RingBuff {
	}
}
