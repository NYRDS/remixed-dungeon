package com.watabou.pixeldungeon.items.scrolls;

import java.util.ArrayList;

import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.buffs.Invisibility;
import com.watabou.pixeldungeon.actors.mobs.Boss;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.Flare;
import com.watabou.utils.Random;

public class ScrollOfDomination extends Scroll {

	@Override
	protected void doRead() {
		
		Sample.INSTANCE.play( Assets.SND_DOMINANCE );
		Invisibility.dispel(curUser);
		
		ArrayList<Mob> mobsInSight = new ArrayList<Mob>();
		
		for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )) {
			if (Dungeon.level.fieldOfView[mob.pos] && !(mob instanceof Boss) && !mob.isPet()) {
				mobsInSight.add(mob);
			}
		}
		
		if(!mobsInSight.isEmpty()) {
			Mob pet = Random.element(mobsInSight);
			Mob.makePet(pet, curUser);
			new Flare( 3, 32 ).show( pet.getSprite(), 2f );
		}
		
		Dungeon.observe();
		
		setKnown();
		
		curUser.spendAndNext( TIME_TO_READ );
	}

	@Override
	public int price() {
		return isKnown() ? 80 * quantity() : super.price();
	}
}
