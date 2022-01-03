package com.nyrds.pixeldungeon.mobs.spiders;

import com.nyrds.pixeldungeon.mobs.common.MobSpawner;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.potions.PotionOfHealing;

public class SpiderNest extends Mob {

	public SpiderNest() {
		
		hp(ht(10));
		baseSpeed = 0f;

		exp = 0;
		maxLvl = 9;
		dmgMin = 0;
		dmgMax = 0;
		dr = 0;

		baseAttackSkill = 1;
		baseDefenseSkill = 2;
		postpone(20);
		
		loot(new PotionOfHealing(), 0.2f);

		movable = false;
	}

	@Override
    public boolean act(){
		super.act();

		MobSpawner.spawnRandomMob(level(), getPos(), 10);
		postpone(20);
		
		return true;
	}

	@Override
	public boolean canBePet() {
		return false;
	}
}
