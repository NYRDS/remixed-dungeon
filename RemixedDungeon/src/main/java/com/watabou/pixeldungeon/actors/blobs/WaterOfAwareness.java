/*
 * Pixel Dungeon
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
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
				
				if (Dungeon.visible[i]) {
					GameScene.discoverTile( i);
				}
			}
		}
		
		Buff.affect( hero, Awareness.class, Awareness.DURATION );
		Dungeon.observe();

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
