
package com.watabou.pixeldungeon.items.weapon.missiles;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.mechanics.buffs.RageBuff;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Amok;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Speed;

public class AmokArrow extends Arrow {

	@LuaInterface
	public AmokArrow() {
		this( 1 );
	}

	public AmokArrow(int number ) {
		super();
		quantity(number);

		image = AMOK_ARROW_IMAGE;
		
		updateStatsForInfo();
	}
	
	@Override
	public int price() {
		return quantity() * 5;
	}

	@Override
	public void attackProc(Char attacker, Char defender, int damage ) {
		if(activateSpecial(attacker, defender, damage)) {
			Buff.affect(defender, Amok.class,20);
			Buff.affect(defender, Speed.class,20);
			Buff rage = new RageBuff();
			rage.attachTo(defender);
		}
		super.attackProc( attacker, defender, damage );
	}
}
