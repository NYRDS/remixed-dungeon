package com.watabou.pixeldungeon.actors.mobs.npcs;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.levels.SewerLevel;
import com.watabou.pixeldungeon.sprites.HedgehogSprite;
import com.watabou.utils.Random;

public class Hedgehog extends NPC {

	{
		spriteClass = HedgehogSprite.class;
		
		flying = true;
		
		state = WANDERING;
	}
	
	@Override
	public float speed() {
		return 0.5f;
	}
	
	@Override
	protected Char chooseEnemy() {
		return DUMMY;
	}
	
	@Override
	public void damage( int dmg, Object src ) {
	}
	
	@Override
	public void add( Buff buff ) {
	}
	
	@Override
	public void interact() {
		// TODO Auto-generated method stub
		
	}
	
	private static boolean spawned;
	
	public static void spawn( SewerLevel level ) {
		if (!spawned && Dungeon.depth > 1 && Random.Int( 5 - Dungeon.depth ) == 0) {
			
			Hedgehog hedgehog = new Hedgehog();
			do {
				hedgehog.pos = level.randomRespawnCell();
			} while (hedgehog.pos == -1);
			level.mobs.add( hedgehog );
			Actor.occupyCell( hedgehog );
			
			spawned = true;
		}
	}

}
