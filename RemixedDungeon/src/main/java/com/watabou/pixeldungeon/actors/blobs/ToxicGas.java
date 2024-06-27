
package com.watabou.pixeldungeon.actors.blobs;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.ResultDescriptions;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Doom;
import com.watabou.pixeldungeon.effects.BlobEmitter;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Random;

public class ToxicGas extends Blob implements Doom {
	
	@Override
	protected void evolve() {
		super.evolve();
		
		for (int pos=0; pos < getLength(); pos++) {
			if (cur[pos] > 0){
				poison(pos);
			}
		}
		
		Blob blob = Dungeon.level.blobs.get( ParalyticGas.class );
		if (blob != null) {
			
			int[] par = blob.cur;
			
			for (int i=0; i < getLength(); i++) {
				
				int t = cur[i];
				int p = par[i];
				
				if (p >= t) {
					setVolume(getVolume() - t);
					cur[i] = 0;
				} else {
					blob.setVolume(blob.getVolume() - p);
					par[i] = 0;
				}
			}
		}
	}
	
	private void poison( int pos ) {
		int levelDamage = 5 + Dungeon.depth * 5;
		
		Char ch = Actor.findChar( pos );
		if (ch != null) {
			int damage = (ch.ht() + levelDamage) / 40;
			if (Random.Int( 40 ) < (ch.ht() + levelDamage) % 40) {
				damage++;
			}
			
			ch.damage( damage, this );
		}
		
		Heap heap = Dungeon.level.getHeap( pos );
		if (heap != null) {
			heap.poison();
		}
	}
	
	@Override
	public void use( BlobEmitter emitter ) {
		super.use( emitter );

		emitter.pour( Speck.factory( Speck.TOXIC ), 0.6f );
	}
	
	@Override
	public String tileDesc() {
        return StringsManager.getVar(R.string.ToxicGas_Info);
    }
	
	@Override
	public void onHeroDeath() {
		
		Badges.validateDeathFromGas();
		
		Dungeon.fail( Utils.format( ResultDescriptions.getDescription(ResultDescriptions.Reason.GAS), Dungeon.depth ) );
        GLog.n(StringsManager.getVar(R.string.ToxicGas_Info1));
	}
}
