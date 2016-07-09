package com.nyrds.pixeldungeon.mobs.necropolis;

import com.nyrds.pixeldungeon.mobs.common.MobSpawner;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.Pushing;
import com.watabou.pixeldungeon.items.potions.PotionOfHealing;
import com.watabou.utils.Callback;

public class JarOfSouls extends UndeadMob {

	public JarOfSouls() {
		hp(ht(60));
		defenseSkill = 1;
		pacified = true;
		
		EXP    = 0;
		maxLvl = 9;

		postpone(20);
		
		loot = new PotionOfHealing();
		lootChance = 0.2f;
	}

	@Override
	public int damageRoll() {
		return 0;
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 0;
	}
	
	@Override
	protected boolean act(){
		super.act();
		PlayZap();

		Mob newMob = MobSpawner.spawnRandomMob(Dungeon.level, getPos());

		int mobPos = Dungeon.level.getEmptyCellNextTo(getPos());

		if (Dungeon.level.cellValid(mobPos)) {
			newMob.setPos(mobPos);
			Actor.addDelayed(new Pushing(newMob, getPos(), newMob.getPos()), -1);
		}

		if(isPet()) {
			Mob.makePet(newMob, Dungeon.hero);
		}

		PlayZap();
		
		postpone(20);
		
		return true;
	}
	
	@Override
	public int dr() {
		return 0;
	}

	@Override
	protected boolean getCloser( int target ) {
		return false;
	}

	@Override
	protected boolean getFurther( int target ) {
		return false;
	}

	public void PlayZap() {
		getSprite().zap(
				getEnemy().getPos(),
				new Callback() {
					@Override
					public void call() {
					}
				});
	}
}
