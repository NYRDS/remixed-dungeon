
package com.watabou.pixeldungeon.items.rings;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;

public class RingOfAccuracy extends Ring {
	
	@Override
	public  ArtifactBuff buff( ) {
		return new Accuracy();
	}
	
	@Override
	public String desc() {
        return isKnown() ? StringsManager.getVar(R.string.RingOfAccuracy_Info) : super.desc();
	}
	
	public class Accuracy extends RingBuff {
		@Override
		public int attackSkillBonus() {
			return level();
		}
	}
}
