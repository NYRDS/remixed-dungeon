
package com.watabou.pixeldungeon.levels.traps;

import com.nyrds.pixeldungeon.levels.objects.ITrigger;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Bleeding;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Cripple;
import com.watabou.pixeldungeon.effects.Wound;

import org.jetbrains.annotations.Nullable;

public class GrippingTrap implements ITrigger {
	
	public static void trigger( int pos, @Nullable Char c ) {
		
		if (c != null) {
			int damage = Math.max( 0,  (Dungeon.depth + 3) - c.defenceRoll(c)/2);
			Buff.affect( c, Bleeding.class ).level( damage );
			Buff.prolong( c, Cripple.class, Cripple.DURATION );
			Wound.hit( c );
		} else {
			Wound.hit( pos );
		}
	}

	@Override
	public void doTrigger(int cell, Char ch) {
		trigger(cell,ch);
	}
}
