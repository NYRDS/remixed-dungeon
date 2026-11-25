package com.watabou.pixeldungeon.actors.buffs;

import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.rings.ArtifactBuff;
import com.watabou.pixeldungeon.ui.BuffIndicator;

/**
 * HeartOfDarkness aura buff (was guts.HeartOfDarkness.HeartOfDarknessBuff):
 * a Spirit of Pain spawns on every defended hit.
 */
public class HeartOfDarknessBuff extends ArtifactBuff {
	@Override
	public int icon() {
		return BuffIndicator.DARKVEIL;
	}

	@Override
	public String name() {
		return StringsManager.getVar(com.nyrds.pixeldungeon.ml.R.string.DarkVeilBuff_Name);
	}

	@Override
	public String desc() {
		return StringsManager.getVar(com.nyrds.pixeldungeon.ml.R.string.DarkVeilBuff_Info);
	}

	@Override
	public int defenceProc(Char defender, Char enemy, int damage) {
		int defenderPos = defender.getPos();
		int spiritPos = defender.level().getEmptyCellNextTo(defenderPos);

		if (defender.level().cellValid(spiritPos)) {
			Mob spirit = MobFactory.mobByName(MobFactory.SPIRIT_OF_PAIN);
			spirit.setPos(spiritPos);
			Mob.makePet(spirit, defender.getId());
			defender.level().spawnMob(spirit, 0, defenderPos);
		}
		return damage;
	}
}
