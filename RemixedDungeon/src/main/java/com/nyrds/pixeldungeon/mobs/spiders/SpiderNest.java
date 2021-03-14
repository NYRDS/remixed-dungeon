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
		
		postpone(20);
		
		loot(new PotionOfHealing(), 0.2f);

		movable = false;
	}
	
	@Override
	public int damageRoll() {
		return 0;
	}

	@Override
    public boolean act(){
		super.act();

		MobSpawner.spawnRandomMob(level(), getPos(), 10);
		postpone(20);
		
		return true;
	}
	
	@Override
	public int dr() {
		return 0;
	}

	@Override
	public boolean canBePet() {
		return false;
	}
}
