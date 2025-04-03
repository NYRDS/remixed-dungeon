package com.nyrds.pixeldungeon.mobs.necropolis;

import com.nyrds.pixeldungeon.mobs.common.MobSpawner;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;

import org.jetbrains.annotations.NotNull;

public class JarOfSouls extends Mob {

	public JarOfSouls() {
		hp(ht(70));
		baseDefenseSkill = 5;
		baseAttackSkill = 1;
		dr = 0;

		dmgMin = 0;
		dmgMax = 0;
		pacified = true;
		
		expForKill = 0;
		maxLvl = 13;

		postpone(20);

		setUndead(true);
		
		//loot = new SoulShard();
		//lootChance = 1f;
	}

	@Override
    public boolean act(){
		super.act();
		if (enemySeen){
			playAttack(getEnemy().getPos());
			MobSpawner.spawnRandomMob(level(),getPos(), -1);
			postpone(15);
		}
		return true;
	}

	@Override
	public boolean getCloser(int target, boolean ignorePets) {
		return false;
	}

	@Override
    public boolean getFurther(int target) {
		return false;
	}

	@Override
	public boolean canBePet() {
		return false;
	}

	@Override
	public boolean zap(@NotNull Char enemy) {
		return false;
	}
}
