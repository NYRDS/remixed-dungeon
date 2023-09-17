
package com.watabou.pixeldungeon.items.wands;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Poison;
import com.watabou.pixeldungeon.effects.MagicMissile;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Callback;

public class WandOfPoison extends SimpleWand  {

	@Override
	protected void onZap( int cell, Char ch ) {
		if (ch != null) {
			Buff.affect( ch, Poison.class,Poison.durationFactor( ch ) * (5 + effectiveLevel()) );
		} else {
            GLog.i(StringsManager.getVar(R.string.WandOfPoison_Info1));
		}
	}
	
	protected void fx( int cell, Callback callback ) {
		MagicMissile.poison( getOwner().getSprite().getParent(), getOwner().getPos(), cell, callback );
		Sample.INSTANCE.play( Assets.SND_ZAP );
	}
	
	@Override
	public String desc() {
        return StringsManager.getVar(R.string.WandOfPoison_Info);
    }
}
