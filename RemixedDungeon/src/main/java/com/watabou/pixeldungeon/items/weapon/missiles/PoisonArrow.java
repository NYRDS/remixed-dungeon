package com.watabou.pixeldungeon.items.weapon.missiles;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Poison;

public class PoisonArrow extends Arrow {

	public static final float DURATION = 5f;

	public PoisonArrow() {
		this(1);
	}

	public PoisonArrow(int number) {
		super();
		quantity(number);

		baseMin = 3;
		baseMax = 4;
		baseDly = 0.75;

		image = POISON_ARROW_IMAGE;

		updateStatsForInfo();
	}

	@Override
	public int price() {
		return quantity() * 5;
	}

	@Override
	public void attackProc(Char attacker, Char defender, int damage) {

		int poisonFactor = 1;

		if (firedFrom != null) {
			poisonFactor += firedFrom.level();
		}
		if (activateSpecial(attacker, defender, damage)) {
			Buff.affect(defender,
					Poison.class,
					Poison.durationFactor(defender) * poisonFactor);
		}
		super.attackProc(attacker, defender, damage);
	}
}
