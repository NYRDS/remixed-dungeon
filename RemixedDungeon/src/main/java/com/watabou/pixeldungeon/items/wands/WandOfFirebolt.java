
package com.watabou.pixeldungeon.items.wands;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.ResultDescriptions;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.Fire;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.effects.MagicMissile;
import com.watabou.pixeldungeon.effects.particles.FlameParticle;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

public class WandOfFirebolt extends SimpleWand  {

	@Override
	protected void onZap( int cell, Char ch ) {

		int wandLevel = effectiveLevel();
		Level level = getOwner().level();

		for (int i=1; i < Ballistica.distance - 1; i++) {
			int c = Ballistica.trace[i];
			if (level.cellValid(c)) {
				if (level.flammable[c]) {
					GameScene.add(Blob.seed(c, 1, Fire.class));
				}
				Heap heap = level.getHeap(c);
				if(heap!=null) {
					heap.burn();
				}
			}
		}
		
		GameScene.add( Blob.seed( cell, 1, Fire.class ) );

		if (ch != null) {
			
			ch.damage( Random.Int( 1, 8 + wandLevel * wandLevel ), this );
			Buff.affect( ch, Burning.class ).reignite( ch );
			
			ch.getSprite().emitter().burst( FlameParticle.FACTORY, 5 );
			
			if (ch == getOwner() && !ch.isAlive()) {
				Dungeon.fail( Utils.format( ResultDescriptions.getDescription(ResultDescriptions.Reason.WAND), name, Dungeon.depth ) );
                GLog.n(StringsManager.getVar(R.string.WandOfFirebolt_Info1));
			}
		}
	}
	
	protected void fx( int cell, Callback callback ) {
		MagicMissile.fire( getOwner().getSprite().getParent(), getOwner().getPos(), cell, callback );
		Sample.INSTANCE.play( Assets.SND_ZAP );
	}
	
	@Override
	public String desc() {
        return StringsManager.getVar(R.string.WandOfFirebolt_Info);
    }
}
