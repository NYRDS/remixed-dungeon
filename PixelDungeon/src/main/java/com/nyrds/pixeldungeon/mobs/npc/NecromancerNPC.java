package com.nyrds.pixeldungeon.mobs.npc;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.npcs.NPC;
import com.watabou.pixeldungeon.items.food.Pasty;
import com.watabou.pixeldungeon.levels.RegularLevel;
import com.watabou.pixeldungeon.sprites.HedgehogSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class NecromancerNPC extends NPC {

	{
		flying = false;
		state = WANDERING;
	}

	String[] phrases = {"Yo", "Sup", "Hey man", "I'd be delighted", "Okami yo waga teki wo kurau!", "Glory to the scourge!"};
	
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
	
	public static void spawn( RegularLevel level ) {
		if (!spawned && Dungeon.depth == 7) {
			NecromancerNPC necro = new NecromancerNPC();
			do {
				necro.setPos(level.randomRespawnCell());
			} while (necro.getPos() == -1);
			level.mobs.add( necro );
			Actor.occupyCell( necro );
			
			spawned = true;
		}
	}

	@Override
	public boolean interact(final Hero hero) {
		getSprite().turnTo( getPos(), hero.getPos() );

		int index = Random.Int(0, phrases.length);
		say(phrases[index]);
		
		return true;
	}

}
