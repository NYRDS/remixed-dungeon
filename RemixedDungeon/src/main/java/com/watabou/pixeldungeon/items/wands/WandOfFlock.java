
package com.watabou.pixeldungeon.items.wands;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.MagicMissile;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.utils.BArray;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;

public class WandOfFlock extends SimpleWand  {

	@Override
	protected void onZap( int cell, Char victim ) {
		int spellLevel = effectiveLevel();
		
		int n = spellLevel + 2;
		
		if (Actor.findChar( cell ) != null && Ballistica.distance > 2) {
			cell = Ballistica.trace[Ballistica.distance - 2];
		}

		Level level = Dungeon.level;

		boolean[] passable = BArray.or( level.passable, level.avoid, null );
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
		
		float lifespan = spellLevel + 3;

	sheepLabel:
		for (int i=0; i < n; i++) {
			do {
				for (int j=0; j < level.getLength(); j++) {
					if (PathFinder.distance[j] == dist) {

						// Sheep kind is lua data now (mobsDesc/Sheep.json, batch 17d-5);
						// the lifetime rides the script via setLifespan
						Mob sheep = MobFactory.mobByName(MobFactory.SHEEP);
						sheep.runInScript("setLifespan", lifespan);
						sheep.setPos(j);
						level.spawnMob(sheep);
						level.press(sheep.getPos(), sheep );

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
}
