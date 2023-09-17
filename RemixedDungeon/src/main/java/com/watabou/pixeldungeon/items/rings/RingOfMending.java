
package com.watabou.pixeldungeon.items.rings;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;

public class RingOfMending extends Ring {
	
	@Override
	public  ArtifactBuff buff( ) {
		return new Rejuvenation();
	}
	
	@Override
	public String desc() {
        return isKnown() ? StringsManager.getVar(R.string.RingOfMending_Info) : super.desc();
	}
	
	public class Rejuvenation extends RingBuff {

		@Override
		public int regenerationBonus() {
			return level();
		}
	}
}
