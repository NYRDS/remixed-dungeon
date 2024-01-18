
package com.watabou.pixeldungeon.items.wands;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Amok;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Vertigo;
import com.watabou.pixeldungeon.effects.MagicMissile;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Callback;

public class WandOfAmok extends SimpleWand {

	@Override
	protected void onZap( int cell, Char victim ) {
		if (victim != null) {
			if (victim  == Dungeon.hero) {
				Buff.affect( victim , Vertigo.class, Vertigo.duration( victim  ) );
			} else {
				Buff.affect( victim , Amok.class, 3f + effectiveLevel() );
			}
		} else {
            GLog.i(StringsManager.getVar(R.string.WandOfAmok_Info1));
		}
	}
	
	protected void fx( int cell, Callback callback ) {
		MagicMissile.purpleLight( getOwner().getSprite().getParent(), getOwner().getPos(), cell, callback );
		Sample.INSTANCE.play( Assets.SND_ZAP );
	}
	
	@Override
	public String desc() {
        return StringsManager.getVar(R.string.WandOfAmok_Info);
    }
}
