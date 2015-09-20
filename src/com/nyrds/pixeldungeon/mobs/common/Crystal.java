package com.nyrds.pixeldungeon.mobs.common;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfPsionicBlast;
import com.watabou.pixeldungeon.items.wands.Wand;
import com.watabou.pixeldungeon.items.wands.WandOfAmok;
import com.watabou.pixeldungeon.items.wands.WandOfAvalanche;
import com.watabou.pixeldungeon.items.wands.WandOfDisintegration;
import com.watabou.pixeldungeon.items.wands.WandOfFirebolt;
import com.watabou.pixeldungeon.items.wands.WandOfLightning;
import com.watabou.pixeldungeon.items.wands.WandOfMagicMissile;
import com.watabou.pixeldungeon.items.wands.WandOfPoison;
import com.watabou.pixeldungeon.items.wands.WandOfRegrowth;
import com.watabou.pixeldungeon.items.wands.WandOfSlowness;
import com.watabou.pixeldungeon.items.wands.WandOfTeleportation;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

public class Crystal extends Mob {

	private int kind;
	
	private static Class[] variants = {	WandOfAmok.class, 
										WandOfAvalanche.class, 
										WandOfDisintegration.class, 
										WandOfFirebolt.class, 
										WandOfLightning.class, 
										WandOfMagicMissile.class, 
										WandOfPoison.class, 
										WandOfRegrowth.class, 
										WandOfSlowness.class};
	
	public Crystal() {
		adjustLevel(Dungeon.depth);
		
		try {
			loot = Random.element(variants).newInstance();
			
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		((Wand)loot).upgrade(Dungeon.depth);
		
		lootChance = 1f;
	}
	
	private void adjustLevel(int depth) {
		kind = Random.Int(0, 1);
		
		hp(ht(50));
		defenseSkill = depth * 2 + 1;
		EXP = depth + 1;
		maxLvl = depth + 2;
		
		IMMUNITIES.add( ScrollOfPsionicBlast.class );
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
		return Ballistica.cast( pos, enemy.pos, false, true ) == enemy.pos;
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
		
		wand.mobWandUse(this, enemy.pos);
		
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
