
package com.watabou.pixeldungeon.items.wands;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.mobs.Boss;
import com.watabou.pixeldungeon.effects.MagicMissile;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Callback;

public class WandOfTeleportation extends Wand {

	public static void teleport(Char ch, Char caster) {
		int pos = ch.level().randomRespawnCell();
		
		if(!ch.level().cellValid(pos)) {
            GLog.i(StringsManager.getVar(R.string.WandOfTeleportation_Info2));
			return;
		}
		
		ch.setPos(pos);
		ch.getSprite().place(ch.getPos());
		ch.getSprite().setVisible(Dungeon.isCellVisible(pos));
        GLog.i(Utils.format(R.string.WandOfTeleportation_Info1, caster.getName(),
				ch.getName_objective()));
	}

	@Override
	protected void onZap(int cell, Char ch) {
		if (ch == getOwner()) {
			setKnown();
			CharUtils.teleportRandom(getOwner());
		} else if (ch != null && ! (ch.isBoss()) && ch.isMovable() ) {
			teleport(ch, getOwner());
		} else {
            GLog.i(StringsManager.getVar(R.string.WandOfTeleportation_Info2));
		}
	}

	protected void fx(int cell, Callback callback) {
		MagicMissile.coldLight(getOwner().getSprite().getParent(), getOwner().getPos(), cell, callback);
		Sample.INSTANCE.play(Assets.SND_ZAP);
	}

	@Override
	public String desc() {
        return StringsManager.getVar(R.string.WandOfTeleportation_Info);
    }
}
