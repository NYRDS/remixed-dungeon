
package com.watabou.pixeldungeon.items.rings;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;

public class RingOfEvasion extends Ring {
	
	@Override
	public  ArtifactBuff buff( ) {
		return new Evasion();
	}
	
	@Override
	public String desc() {
        return isKnown() ? StringsManager.getVar(R.string.RingOfEvasion_Info) : super.desc();
	}
	
	public class Evasion extends RingBuff {
		@Override
		public int defenceSkillBonus(Char chr) {
			return level();
		}
	}
}
