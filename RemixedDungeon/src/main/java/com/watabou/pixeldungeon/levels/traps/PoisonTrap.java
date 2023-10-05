
package com.watabou.pixeldungeon.levels.traps;

import com.nyrds.pixeldungeon.levels.objects.ITrigger;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Poison;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.particles.PoisonParticle;

import org.jetbrains.annotations.Nullable;

public class PoisonTrap implements ITrigger {

	// 0xBB66EE
	
	public static void trigger( int pos, @Nullable Char ch ) {
		if (ch == null){
			ch = Actor.findChar(pos);
		}
		if (ch != null) {
			Buff.affect( ch, Poison.class,Poison.durationFactor( ch ) * (4 + Dungeon.depth / 2) );
		}
		CellEmitter.center( pos ).burst( PoisonParticle.SPLASH, 3 );
	}

	@Override
	public void doTrigger(int cell, Char ch) {
		trigger(cell,ch);
	}
}
