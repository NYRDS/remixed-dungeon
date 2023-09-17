
package com.watabou.pixeldungeon.items.rings;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;

public class RingOfPower extends Ring {
	
	@Override
	public ArtifactBuff buff( ) {
		return new Power();
	}
	
	@Override
	public String desc() {
        return isKnown() ? StringsManager.getVar(R.string.RingOfPower_Info) : super.desc();
	}
	
	public class Power extends RingBuff {
	}
}
