package com.watabou.pixeldungeon.actors.mobs.npcs;

import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.food.Pasty;
import com.watabou.pixeldungeon.levels.RegularLevel;
import com.watabou.pixeldungeon.sprites.HedgehogSprite;
import com.watabou.utils.Bundle;

public class Hedgehog extends NPC {

	{
		spriteClass = HedgehogSprite.class;
		setState(WANDERING);
	}
	
	@Override
	public float speed() {
		return speed;
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
	
	private static boolean spawned;
	
	private static String action_tag = "action";
	private static String speed_tag  = "speed";
	
	private int     action = 0;
	private  float  speed  = 0.5f;
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle(bundle);
		bundle.put(action_tag, action);
		bundle.put(speed_tag,  speed);
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		action = bundle.getInt(action_tag);
		speed  = bundle.getFloat(speed_tag);
	}
	
	public static void spawn( RegularLevel level ) {
		if (!spawned && Dungeon.depth == 23) {
			Hedgehog hedgehog = new Hedgehog();
			do {
				hedgehog.setPos(level.randomRespawnCell());
			} while (hedgehog.getPos() == -1);
			level.mobs.add( hedgehog );
			Actor.occupyCell( hedgehog );
			
			spawned = true;
		}
	}

	@Override
	public boolean interact(final Hero hero) {
		getSprite().turnTo( getPos(), hero.getPos() );
		
		switch (action)
		{
			case 0:
				say(Game.getVar(R.string.Hedgehog_Info1));
			break;
		
			case 1:
				say(Game.getVar(R.string.Hedgehog_Info2));
			break;
			
			case 2:
				say(Game.getVar(R.string.Hedgehog_Info3));
			break;
			
			case 3:
				say(Game.getVar(R.string.Hedgehog_Info4));
				
				Pasty pie = new Pasty();
				
				Dungeon.level.drop( pie, getPos() ).sprite.drop();
			break;
			
			default:
				say(Game.getVar(R.string.Hedgehog_ImLate));
				action = 4;
				speed  = 3;
		}
		speed += 0.5f;
		action++;
		
		return true;
	}

}
