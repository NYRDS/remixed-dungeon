package com.nyrds.pixeldungeon.mobs.common;

import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.utils.Bundle;

public class MultiKindMob extends Mob {
	
	protected int kind = 0;
	
	@Override
	public int getKind() {
		return kind;
	}
	
	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put("KIND", kind);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		kind = bundle.optInt("KIND", 0);
	}
	
}
