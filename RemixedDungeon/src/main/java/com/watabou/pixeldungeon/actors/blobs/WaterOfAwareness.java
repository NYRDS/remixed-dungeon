
package com.watabou.pixeldungeon.actors.blobs;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.Journal;
import com.watabou.pixeldungeon.Journal.Feature;
import com.watabou.pixeldungeon.actors.buffs.Awareness;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.effects.BlobEmitter;
import com.watabou.pixeldungeon.effects.Identification;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.levels.TerrainFlags;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.GLog;

public class WaterOfAwareness extends WellWater {

	@Override
	protected boolean affectHero( Hero hero ) {
		
		Sample.INSTANCE.play( Assets.SND_DRINK );
		emitter.getParent().add( new Identification( DungeonTilemap.tileCenterToWorld( pos ) ) );
		
		hero.getBelongings().observe();
		Level level = hero.level();

		for (int i=0; i < level.getLength(); i++) {
			
			int terr = level.map[i];
			if ((TerrainFlags.flags[terr] & TerrainFlags.SECRET) != 0) {
				
				level.set( i, Terrain.discover( terr ) );
				GameScene.updateMapPair(i);
				
				if (Dungeon.isCellVisible(i)) {
					GameScene.discoverTile( i);
				}
			}
		}
		
		Buff.affect( hero, Awareness.class, Awareness.DURATION );
		hero.observe();

		hero.interrupt();

        GLog.p(StringsManager.getVar(R.string.WaterOfAwareness_Procced));

		Journal.remove( Feature.WELL_OF_AWARENESS.desc() );
		
		return true;
	}
	
	@Override
	protected Item affectItem( Item item ) {
		if (item.isIdentified()) {
			return null;
		} else {
			item.identify();
			Badges.validateItemLevelAcquired( item );
			
			emitter.getParent().add( new Identification( DungeonTilemap.tileCenterToWorld( pos ) ) );
			
			Journal.remove( Feature.WELL_OF_AWARENESS.desc() );
			
			return item;
		}
	}
	
	@Override
	public void use( BlobEmitter emitter ) {
		super.use( emitter );	
		emitter.pour( Speck.factory( Speck.QUESTION ), 0.3f );
	}
	
	@Override
	public String tileDesc() {
        return StringsManager.getVar(R.string.WaterOfAwareness_Info);
    }
}
