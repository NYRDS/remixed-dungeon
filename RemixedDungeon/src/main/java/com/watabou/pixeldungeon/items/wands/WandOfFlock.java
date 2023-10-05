
package com.watabou.pixeldungeon.items.wands;

import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.npcs.NPC;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.MagicMissile;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.sprites.SheepSprite;
import com.watabou.pixeldungeon.utils.BArray;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class WandOfFlock extends SimpleWand  {

	@Override
	protected void onZap( int cell, Char victim ) {
		int level = effectiveLevel();
		
		int n = level + 2;
		
		if (Actor.findChar( cell ) != null && Ballistica.distance > 2) {
			cell = Ballistica.trace[Ballistica.distance - 2];
		}
		
		boolean[] passable = BArray.or( Dungeon.level.passable, Dungeon.level.avoid, null );
		for (Actor actor : Actor.all()) {
			if (actor instanceof Char) {
				passable[((Char)actor).getPos()] = false;
			}
		}
		
		PathFinder.buildDistanceMap( cell, passable, n );
		int dist = 0;
		
		if (Actor.findChar( cell ) != null) {
			PathFinder.distance[cell] = Integer.MAX_VALUE;
			dist = 1;
		}
		
		float lifespan = level + 3;
		
	sheepLabel:
		for (int i=0; i < n; i++) {
			do {
				for (int j=0; j < Dungeon.level.getLength(); j++) {
					if (PathFinder.distance[j] == dist) {
						
						Sheep sheep = new Sheep();
						sheep.lifespan = lifespan;
						sheep.setPos(j);
						Dungeon.level.spawnMob(sheep);
						Dungeon.level.press(sheep.getPos(), sheep );
						
						CellEmitter.get( j ).burst( Speck.factory( Speck.WOOL ), 4 );
						
						PathFinder.distance[j] = Integer.MAX_VALUE;
						
						continue sheepLabel;
					}
				}
				dist++;
			} while (dist < n);
		}
	}
	
	protected void fx( int cell, Callback callback ) {
		MagicMissile.wool( getOwner().getSprite().getParent(), getOwner().getPos(), cell, callback );
		Sample.INSTANCE.play( Assets.SND_ZAP );
	}

	@Override
	public String desc() {
        return StringsManager.getVar(R.string.WandOfFlock_Info);
    }

	public static class Sheep extends NPC {
		
		public Sheep() {
			super();
			spriteClass = SheepSprite.class;
		}

		public float lifespan;
		
		private boolean initialized = false;
		
		@Override
        public boolean act() {
			if (initialized) {
				hp(0);

				destroy();
				getSprite().die();
				
			} else {
				initialized = true;
				spend( lifespan + Random.Float( 2 ) );
			}
			return true;
		}
		
		@Override
		public void damage(int dmg, @NotNull NamedEntityKind src ) {
		}

		@Override
		public boolean interact(final Char hero) {
            say( Random.element(StringsManager.getVars(R.array.WandOfFlock_SheepBaa)) );
			return false;
		}
	}
}
