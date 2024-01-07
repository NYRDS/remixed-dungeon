package com.nyrds.pixeldungeon.mobs.icecaves;

import com.nyrds.pixeldungeon.items.Treasury;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.common.IZapper;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Slow;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.weapon.enchantments.Death;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class KoboldIcemancer extends Mob implements IZapper {

	public KoboldIcemancer() {
		hp(ht(70));
		baseDefenseSkill = 18;
		baseAttackSkill  = 25;
		dmgMin = 15;
		dmgMax = 17;
		dr = 11;

		expForKill = 11;
		maxLvl = 21;

		loot(Treasury.Category.POTION,  0.83f);

		addResistance(Death.class);
	}

	@Override
    public boolean canAttack(@NotNull Char enemy) {
		return Ballistica.cast(getPos(), enemy.getPos(), false, true) == enemy.getPos();
	}

	@Override
	public boolean zap(@NotNull Char enemy) {
		if(zapHit(enemy)) {
			if (Random.Int(2) == 0) {
				Buff.prolong( enemy, Slow.class, 1 );
			}

            CharUtils.checkDeathReport(this ,enemy, StringsManager.getVar(R.string.KoboldIcemancer_Killed));
			return true;
		}
		return false;
	}
}
