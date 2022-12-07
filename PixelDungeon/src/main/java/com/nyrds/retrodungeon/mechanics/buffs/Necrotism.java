package com.nyrds.retrodungeon.mechanics.buffs;

import com.nyrds.Packable;
import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.ResultDescriptions;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Boss;
import com.watabou.pixeldungeon.actors.mobs.npcs.NPC;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.ui.BuffIndicator;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class Necrotism extends Buff implements Hero.Doom {

	private static final String LEFT	= "left";

	protected float left;

	@Packable
	protected int iteration;

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

	public void set( float duration, int i ) {
		this.left = duration + i;
		iteration = i;
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

			target.getSprite().burst( 0x6935a5, 3 );

			int damage;
			if (target instanceof Boss){
				damage = (target.hp()/200);
			} else{
				damage = ( target.hp() / Math.max(3, (21 - iteration)) );
			}

			target.damage( Math.max(1, damage * iteration), this );
			spend( TICK );

			int cell = target.getPos();

			for (int n : Level.NEIGHBOURS16) {
				int p = n + cell;
				Char ch = Actor.findChar(p);
				if (Dungeon.level.cellValid(p) && ch != null && !(ch instanceof  Hero) && !(ch instanceof NPC)){
					if(Random.Int(1) == 0 && ch.buff(Necrotism.class) == null){
						Buff.affect( ch, Necrotism.class ).set(duration, iteration + 1);
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

	@Override
	public void onDeath() {
		Badges.validateDeathFromNecrotism();

		Dungeon.fail( Utils.format( ResultDescriptions.NECROTISM, Dungeon.depth ) );
		GLog.n(Game.getVar(R.string.Necrotism_Death));
	}
}
