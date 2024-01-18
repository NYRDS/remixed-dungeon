
package com.watabou.pixeldungeon.items.wands;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Slow;
import com.watabou.pixeldungeon.effects.MagicMissile;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Callback;

public class WandOfSlowness extends SimpleWand  {
	
	@Override
	protected void onZap( int cell, Char ch ) {
		if (ch != null) {
			Buff.affect( ch, Slow.class, Slow.duration( ch ) / 3 + effectiveLevel() );
		} else {
            GLog.i(StringsManager.getVar(R.string.WandOfSlowness_Info1));
		}
	}
	
	protected void fx( int cell, Callback callback ) {
		MagicMissile.slowness( getOwner().getSprite().getParent(), getOwner().getPos(), cell, callback );
		Sample.INSTANCE.play( Assets.SND_ZAP );
	}
	
	@Override
	public String desc() {
        return StringsManager.getVar(R.string.WandOfSlowness_Info);
    }
}
