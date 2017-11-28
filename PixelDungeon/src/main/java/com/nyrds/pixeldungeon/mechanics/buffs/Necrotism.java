package com.nyrds.pixeldungeon.mechanics.buffs;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.ResultDescriptions;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.rings.RingOfElements;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.ui.BuffIndicator;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class Necrotism extends Buff implements Hero.Doom {

	protected float left;

	private static final String LEFT	= "left";

	public static int duration = 3;

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( LEFT, left );

	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		left = bundle.getFloat( LEFT );
	}

	public void set( float duration ) {
		this.left = duration;
	}

	@Override
	public int icon() {
		return BuffIndicator.NECROTISM;
	}

	@Override
	public String toString() {
		return Game.getVar(R.string.Necrotism_Info);
	}

	@Override
	public boolean act() {
		if (target.isAlive()) {

			target.damage( Math.max(1,target.hp()/20), this );
			spend( TICK );

			int cell = target.getPos();

			for (int n : Level.NEIGHBOURS8) {
				int p = n + cell;
				Char ch = Actor.findChar(p);
				if (Dungeon.level.cellValid(p) && ch != null){
					if(Random.Int(1) == 0){
						Buff.affect( ch, Necrotism.class ).set(duration);
					}
				}
			}

			if ((left -= TICK) <= 0) {
				detach();
			}

		} else {

			detach();

		}

		return true;
	}

	public static float durationFactor( Char ch ) {
		RingOfElements.Resistance r = ch.buff( RingOfElements.Resistance.class );
		return r != null ? r.durationFactor() : 1;
	}

	@Override
	public void onDeath() {
		Badges.validateDeathFromPoison();

		Dungeon.fail( Utils.format( ResultDescriptions.POISON, Dungeon.depth ) );
		GLog.n(Game.getVar(R.string.Poison_Death));
	}
}
