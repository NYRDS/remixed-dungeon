package com.watabou.pixeldungeon.items.scrolls;

import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.buffs.Invisibility;
import com.watabou.pixeldungeon.actors.mobs.Mob;

public class ScrollOfDomination extends Scroll {

	@Override
	protected void doRead() {
		
		Sample.INSTANCE.play( Assets.SND_DOMINANCE );
		Invisibility.dispel(curUser);
		
		for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )) {
			if (Dungeon.level.fieldOfView[mob.pos]) {
				Mob.makePet(mob);
				break;
			}
		}
		Dungeon.observe();
		
		setKnown();
		
		curUser.spendAndNext( TIME_TO_READ );
	}
	/*
	@Override
	public String desc() {
		return Game.getVar(R.string.ScrollOfDominance_Info);
	}
	*/
	@Override
	public int price() {
		return isKnown() ? 80 * quantity() : super.price();
	}
}
