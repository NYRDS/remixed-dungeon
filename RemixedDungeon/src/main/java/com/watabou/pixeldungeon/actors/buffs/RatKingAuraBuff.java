package com.watabou.pixeldungeon.actors.buffs;

import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.rings.ArtifactBuff;
import com.watabou.pixeldungeon.ui.BuffIndicator;

/**
 * RatKingCrown aura buff (was common.RatKingCrown.RatKingAuraBuff): rats the
 * wearer strikes become pets.
 */
public class RatKingAuraBuff extends ArtifactBuff {
	@Override
	public int icon() {
		return BuffIndicator.RATTNESS;
	}

	@Override
	public String name() {
		return StringsManager.getVar(com.nyrds.pixeldungeon.ml.R.string.RatKingCrownBuff_Name);
	}

	@Override
	public String desc() {
		return StringsManager.getVar(com.nyrds.pixeldungeon.ml.R.string.RatKingCrownBuff_Info);
	}

	@Override
	public int attackProc(Char attacker, Char defender, int damage) {
		if (defender.getEntityKind().equals(MobFactory.RAT) && attacker.buffLevel(getEntityKind()) > 0) {
			Mob.makePet((Mob) defender, attacker.getId());
		}
		return super.attackProc(attacker, defender, damage);
	}
}
