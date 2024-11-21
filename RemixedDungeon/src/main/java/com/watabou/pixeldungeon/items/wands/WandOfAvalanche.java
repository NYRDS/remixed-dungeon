
package com.watabou.pixeldungeon.items.wands;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.noosa.Camera;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.ResultDescriptions;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Stun;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.MagicMissile;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.nyrds.util.BArray;
import com.watabou.pixeldungeon.utils.GLog;
import com.nyrds.util.Utils;
import com.nyrds.util.Callback;
import com.watabou.utils.PathFinder;
import com.nyrds.util.Random;

public class WandOfAvalanche extends SimpleWand {
	{
		hitChars = false;
	}
	
	@Override
	protected void onZap( int cell, Char victim ) {
		
		Sample.INSTANCE.play( Assets.SND_ROCKS );
		
		int level = effectiveLevel();
		
		Ballistica.distance = Math.min( Ballistica.distance, 8 + level );
		
		int size = 1 + level / 3;
		PathFinder.buildDistanceMap( cell, BArray.not( Dungeon.level.solid, null ), size );
		
		for (int i=0; i < Dungeon.level.getLength(); i++) {
			
			int d = PathFinder.distance[i];
			
			if (d < Integer.MAX_VALUE) {
				
				Char ch = Actor.findChar( i ); 
				if (ch != null) {
					
					ch.getSprite().flash();
					
					ch.damage( Random.Int( 2, 6 + (size - d) * 2 ), this );
					
					if (ch.isAlive() && Random.Int( 2 + d ) == 0) {
						Buff.prolong( ch, Stun.class, Random.IntRange( 2, 6 ) );
					}
				}

				CellEmitter.get( i ).start( Speck.factory( Speck.ROCK ), 0.07f, 3 + (size - d) );
				Camera.main.shake( 3, 0.07f * (3 + (size - d)) );
			}
		}
		
		if ((getOwner() instanceof Hero)  && !getOwner().isAlive()) {
			Dungeon.fail( Utils.format( ResultDescriptions.getDescription(ResultDescriptions.Reason.WAND), name, Dungeon.depth ) );
            GLog.n(StringsManager.getVar(R.string.WandOfAvalanche_Info1));
		}
	}
	
	protected void fx( int cell, Callback callback ) {
		MagicMissile.earth( getOwner().getSprite().getParent(), getOwner().getPos(), cell, callback );
		Sample.INSTANCE.play( Assets.SND_ZAP );
	}
	
	@Override
	public String desc() {
        return StringsManager.getVar(R.string.WandOfAvalanche_Info);
    }
}
