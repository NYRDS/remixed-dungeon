
package com.watabou.pixeldungeon.items.wands;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.Regrowth;
import com.watabou.pixeldungeon.effects.MagicMissile;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Callback;

public class WandOfRegrowth extends SimpleWand  {
	
	@Override
	protected void onZap( int cell, Char ch ) {

		final Level level = Dungeon.level;

		for (int i = 1; i < Ballistica.distance-1; i++) {
			int p = Ballistica.trace[i];
			int c = level.map[p];
			if (c == Terrain.EMPTY || 
				c == Terrain.EMBERS || 
				c == Terrain.EMPTY_DECO) {
				
				level.set( p, Terrain.GRASS );
				
			}
		}
		
		int c = level.map[cell];
		if (c == Terrain.EMPTY || 
			c == Terrain.EMBERS || 
			c == Terrain.EMPTY_DECO || 
			c == Terrain.GRASS ||
			c == Terrain.HIGH_GRASS) {
			
			GameScene.add( Blob.seed( cell, (effectiveLevel() + 2) * 20, Regrowth.class ) );
			
		} else {
            GLog.i(StringsManager.getVar(R.string.WandOfRegrowth_Info1));
		}
	}
	
	protected void fx( int cell, Callback callback ) {
		MagicMissile.foliage( getOwner().getSprite().getParent(), getOwner().getPos(), cell, callback );
		Sample.INSTANCE.play( Assets.SND_ZAP );
	}
	
	@Override
	public String desc() {
        return StringsManager.getVar(R.string.WandOfRegrowth_Info);
    }
}
