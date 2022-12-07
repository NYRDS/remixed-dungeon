package com.nyrds.retrodungeon.mobs.spiders;

import android.support.annotation.NonNull;

import com.nyrds.retrodungeon.mobs.common.MultiKindMob;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Blindness;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Charm;
import com.watabou.pixeldungeon.actors.buffs.FlavourBuff;
import com.watabou.pixeldungeon.actors.buffs.Levitation;
import com.watabou.pixeldungeon.actors.buffs.Roots;
import com.watabou.pixeldungeon.actors.buffs.Slow;
import com.watabou.pixeldungeon.actors.buffs.Vertigo;
import com.watabou.pixeldungeon.actors.buffs.Weakness;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.food.MysteryMeat;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.utils.Random;

public class SpiderMindAmber extends MultiKindMob {

	static Class<?> BuffsForEnemy[] = {
		Blindness.class,
		Charm.class,
		Levitation.class,
		Roots.class,
		Slow.class,
		Vertigo.class,
		Weakness.class
	};

	public SpiderMindAmber() {
		hp(ht(30));
		defenseSkill = 5;
		baseSpeed = 0.9f;
		
		exp = 9;
		maxLvl = 10;

		kind = 1;

		loot = new MysteryMeat();
		lootChance = 0.067f;
	}
	
	@Override
	protected boolean canAttack( Char enemy ) {
		return !Dungeon.level.adjacent( getPos(), enemy.getPos() ) && Ballistica.cast( getPos(), enemy.getPos(), false, true ) == enemy.getPos();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public int attackProc(@NonNull Char enemy, int damage ) {
		
		if(enemy instanceof Hero) {
			Class <? extends FlavourBuff> buffClass = (Class<? extends FlavourBuff>) Random.oneOf(BuffsForEnemy);		
			Buff.prolong( enemy, buffClass, 3 );
		}
		
		return damage;
	}
	
	@Override
	protected boolean getCloser( int target ) {
		if (getState() == HUNTING) {
			return enemySeen && getFurther( target );
		} else {
			return super.getCloser( target );
		}
	}
	
	@Override
	public int damageRoll() {
		return 1;
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 15;
	}
	
	@Override
	public int dr() {
		return 10;
	}

	@Override
	public void die( Object cause ) {
		super.die( cause );
		Badges.validateRare( this );
	}

	@Override
	public boolean zap(@NonNull Char enemy) {
		attackProc(enemy, damageRoll());
		super.zap(enemy);
		return true;
	}

}
