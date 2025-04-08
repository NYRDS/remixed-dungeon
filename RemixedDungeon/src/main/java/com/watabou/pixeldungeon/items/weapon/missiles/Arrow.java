package com.watabou.pixeldungeon.items.weapon.missiles;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.weapon.melee.KindOfBow;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public abstract class Arrow extends MissileWeapon {

	
	protected final int COMMON_ARROW_IMAGE    = 7;
	protected final int FIRE_ARROW_IMAGE      = 8;
	protected final int POISON_ARROW_IMAGE    = 9;
	protected final int PARALYSIS_ARROW_IMAGE = 10;
	protected final int FROST_ARROW_IMAGE     = 6;
	protected final int HEALTH_ARROW_IMAGE    = 13;
	protected final int AMOK_ARROW_IMAGE      = 15;

	protected final double baseAcu = 1;
	protected double baseDly = 1;
	protected double baseMax = 1;
	protected double baseMin = 1;

	protected KindOfBow firedFrom;

	public Arrow() {
		this(1);
	}

	public Arrow(int number) {
		super();
		setSTR(9);
		quantity(number);

	}

	protected void updateStatsForInfo() {
		MAX = (int) baseMax;
		MIN = (int) baseMin;
		ACU = (float) baseAcu;
		DLY = (float) baseDly;
	}

	protected boolean activateSpecial(Char attacker, Char defender, int damage) {
		if (firedFrom != null) {
			return true;
		}

		return Random.Float(1f) < 0.25f;
	}

	@Override
	public Item random() {
		quantity(Random.Int(15, 30));
		return this;
	}

	@Override
	protected void onThrow(int cell, @NotNull Char thrower,Char enemy) {
		if (thrower.bowEquipped()) {

			if (thrower.level().adjacent(thrower.getPos(), cell)
					&& thrower.getHeroClass() != HeroClass.ELF) {
				miss(cell, thrower, enemy);
				return;
			}

			firedFrom = (KindOfBow) thrower.getItemFromSlot(Belongings.Slot.WEAPON);

			MAX = (int) (baseMax * firedFrom.dmgFactor());
			MIN = (int) (baseMin * firedFrom.dmgFactor());
			ACU = (float) (baseAcu * firedFrom.acuFactor());
			DLY = (float) (baseDly * firedFrom.dlyFactor());

			float sDelta = thrower.effectiveSTR() - firedFrom.requiredSTR();

			if (sDelta > 2) {
				MAX += MIN + sDelta;
			}

			if (thrower.getHeroClass() == HeroClass.ELF) {
				ACU *= 1.1f;
				DLY *= 0.9f;
			}

			firedFrom.usedForHit();
			firedFrom.useArrowType(this);

			super.onThrow(cell, thrower, enemy);
		} else {
			miss(cell, thrower, enemy);
		}
	}

	@Override
	public Item burn(int cell) {
		return null;
	}
	
	@Override
	public String imageFile() {
		return "items/ammo.png";
	}
}
