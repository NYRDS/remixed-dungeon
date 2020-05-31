package com.nyrds.pixeldungeon.mobs.icecaves;

import com.nyrds.pixeldungeon.items.Treasury;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.common.IZapper;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
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
		defenseSkill = 18;

		exp = 11;
		maxLvl = 21;

		loot = Treasury.Category.POTION;
		lootChance = 0.83f;

		addResistance(Death.class);
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange(15, 17);
	}

	@Override
	public int attackSkill(Char target) {
		return 25;
	}

	@Override
	public int dr() {
		return 11;
	}

	@Override
    public boolean canAttack(@NotNull Char enemy) {
		return Ballistica.cast(getPos(), enemy.getPos(), false, true) == enemy.getPos();
	}

	@Override
	public boolean zap(@NotNull Char enemy) {
		if(zapHit(enemy)) {

			if (enemy == Dungeon.hero && Random.Int(2) == 0) {
				Buff.prolong( enemy, Slow.class, 1 );
			}

			enemy.damage(damageRoll(), this);

			CharUtils.checkDeathReport(this ,enemy, Game.getVar(R.string.KoboldIcemancer_Killed));
			return true;
		}
		return false;
	}
}
