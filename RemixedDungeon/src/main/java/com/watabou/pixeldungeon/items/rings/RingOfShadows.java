
package com.watabou.pixeldungeon.items.rings;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;

public class RingOfShadows extends Ring {
	
	@Override
	public ArtifactBuff buff( ) {
		return new Shadows();
	}
	
	@Override
	public String desc() {
        return isKnown() ? StringsManager.getVar(R.string.RingOfShadows_Info) : super.desc();
	}
	
	public class Shadows extends RingBuff {
		@Override
		public int stealthBonus() {
			return level();
		}
	}
}
