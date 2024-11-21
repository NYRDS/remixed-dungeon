
package com.watabou.pixeldungeon.actors.buffs;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.ResultDescriptions;
import com.watabou.pixeldungeon.ui.BuffIndicator;
import com.watabou.pixeldungeon.utils.GLog;
import com.nyrds.util.Utils;

public class Ooze extends Buff {

	@Override
	public int icon() {
		return BuffIndicator.OOZE;
	}

	@Override
	public boolean act() {
		if (target.isAlive()) {
			target.damage( level, this );
			if (!target.isAlive() && target == Dungeon.hero) {
				Dungeon.fail( Utils.format( ResultDescriptions.getDescription(ResultDescriptions.Reason.OOZE), Dungeon.depth ) );
                GLog.n(StringsManager.getVar(R.string.Ooze_Death), name() );
			}
			spend( TICK );
		}
		if (target.level().water[target.getPos()]) {
			if (level--<= 0) {
				detach();
			}
		}
		return true;
	}
}
