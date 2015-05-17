package com.watabou.pixeldungeon.actors.mobs;

import java.util.HashSet;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.items.food.RottenPasty;
import com.watabou.pixeldungeon.sprites.MimicPieSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class MimicPie extends Mob {
	
	private int level;
	
	public MimicPie() {
		spriteClass = MimicPieSprite.class;
		
		baseSpeed = 1.25f;
		
		level = Dungeon.depth;
	}
	
	private static final String LEVEL	= "level";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( LEVEL, level );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		adjustStats( bundle.getInt( LEVEL ) );
	}
	
	@Override
	public int damageRoll() {
		return Random.NormalIntRange( ht() / 10, ht() / 4 );
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 9 + level;
	}
	
	@Override
	public void die(Object cause) {
		super.die(cause);
		Dungeon.level.drop(new RottenPasty(), pos);
	}
	
	public void adjustStats( int level ) {
		this.level = level;
		
		hp(ht((3 + level) * 5));
		EXP = 2 + 2 * (level - 1) / 5;
		defenseSkill = 2 * attackSkill( null ) / 3;
		
		enemySeen = true;
	}
		
	private static final HashSet<Class<?>> IMMUNITIES = new HashSet<Class<?>>();
	static {
		IMMUNITIES.add( ToxicGas.class );
		IMMUNITIES.add( Paralysis.class );
	}
	
	@Override
	public HashSet<Class<?>> immunities() {
		return IMMUNITIES;
	}
}
