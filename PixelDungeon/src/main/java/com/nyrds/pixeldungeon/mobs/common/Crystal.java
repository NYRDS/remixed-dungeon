package com.nyrds.pixeldungeon.mobs.common;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.ParalyticGas;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfPsionicBlast;
import com.watabou.pixeldungeon.items.wands.SimpleWand;
import com.watabou.pixeldungeon.items.wands.Wand;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.utils.Random;

public class Crystal extends Mob {

	private int kind;

	static private int ctr = 0;
	
	public Crystal() {
		adjustLevel(Dungeon.depth);
		
		loot = SimpleWand.createRandomSimpleWand();
		((Wand)loot).upgrade(Dungeon.depth);
		
		lootChance = 0.25f;
	}

	static public Crystal makeShadowLordCrystal() {
		Crystal crystal = new Crystal();
		crystal.kind = 2;
		crystal.lootChance = 0;

		return crystal;
	}

	private void adjustLevel(int depth) {
		kind = (ctr++)%2;
		
		hp(ht(Dungeon.depth * 4 + 1));
		defenseSkill = depth * 2 + 1;
		EXP = depth + 1;
		maxLvl = depth + 2;
		
		IMMUNITIES.add( ScrollOfPsionicBlast.class );
		IMMUNITIES.add( ToxicGas.class );
		IMMUNITIES.add( ParalyticGas.class );
	}
	
	@Override
	public int getKind() {
		return kind;
	}
	
	@Override
	public int damageRoll() {
		return Random.NormalIntRange( hp() / 2, ht() / 2 );
	}
	
	@Override
	protected boolean canAttack( Char enemy ) {
		return Ballistica.cast( getPos(), enemy.getPos(), false, true ) == enemy.getPos();
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 1000;
	}
	
	@Override
	public int dr() {
		return EXP / 3;
	}

	@Override
	public int attackProc( final Char enemy, int damage ) {
		final Wand wand = ((Wand)loot);
		
		wand.mobWandUse(this, enemy.getPos());
		
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
	
	

}
