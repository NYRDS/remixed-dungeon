package com.nyrds.pixeldungeon.mobs.necropolis;

import com.nyrds.pixeldungeon.mobs.common.MobSpawner;
import com.nyrds.pixeldungeon.mobs.spiders.SpiderSpawner;
import com.nyrds.pixeldungeon.mobs.spiders.sprites.SpiderNestSprite;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Amok;
import com.watabou.pixeldungeon.actors.buffs.Blindness;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.buffs.Sleep;
import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.potions.PotionOfHealing;
import com.watabou.pixeldungeon.items.weapon.enchantments.Death;
import com.watabou.utils.Callback;

//Supposed to periodically summon undead creatures
public class JarOfSouls extends UndeadMob {

	public JarOfSouls() {
		hp(ht(60));
		defenseSkill = 1;
		baseSpeed = 0f;
		
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
		
		Mob newMob = MobSpawner.spawnRandomMob(Dungeon.level, getPos());

		if(isPet()) {
			Mob.makePet(newMob, Dungeon.hero);
		}

		PlayZap();
		state = SLEEPING;
		
		postpone(20);
		
		return true;
	}
	
	@Override
	public int dr() {
		return 0;
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
