
package com.watabou.pixeldungeon.items.rings;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;

public class RingOfHaste extends Ring {
	
	@Override
	public  ArtifactBuff buff( ) {
		return new Haste();
	}
	
	@Override
	public String desc() {
        return isKnown() ? StringsManager.getVar(R.string.RingOfHaste_Info) : super.desc();
	}
	
	public class Haste extends RingBuff {
		@Override
		public float hasteLevel() {
			return level();
		}
	}
}
