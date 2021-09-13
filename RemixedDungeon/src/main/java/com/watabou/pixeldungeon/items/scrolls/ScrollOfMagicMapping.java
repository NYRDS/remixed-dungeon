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
package com.watabou.pixeldungeon.items.scrolls;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Invisibility;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.effects.SpellSprite;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.levels.TerrainFlags;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.GLog;

import org.jetbrains.annotations.NotNull;

import lombok.var;

public class ScrollOfMagicMapping extends Scroll {

	@Override
	protected void doRead(@NotNull Char reader) {
		Level level = reader.level();

		int length = level.getLength();
		int[] map = level.map;
		boolean[] mapped = level.mapped;
		boolean[] discoverable = level.discoverable;
		
		boolean noticed = false;
		
		for (int i=0; i < length; i++) {
			int terr = map[i];
			if (discoverable[i]) {
				mapped[i] = true;
				if ((TerrainFlags.flags[terr] & TerrainFlags.SECRET) != 0) {
					
					level.set( i, Terrain.discover( terr ) );
					GameScene.updateMap( i );
					
					if (Dungeon.isCellVisible(i)) {
						GameScene.discoverTile( i);
						discover( i );
						
						noticed = true;
					}
				}
			}
		}

		for(var lo : level.getAllLevelObjects()) {
			if(lo.secret()) {
				lo.discover();
				discover(lo.getPos());
				noticed = true;
			}
		}

		Dungeon.observe();

        GLog.i(StringsManager.getVar(R.string.ScrollOfMagicMapping_Layout));

		if (noticed) {
			Sample.INSTANCE.play( Assets.SND_SECRET );
		}
		
		SpellSprite.show( reader, SpellSprite.MAP );
		Sample.INSTANCE.play( Assets.SND_READ );
		Invisibility.dispel(reader);
		
		setKnown();

		reader.spend( TIME_TO_READ );
	}
	
	@Override
	public int price() {
		return isKnown() ? 25 * quantity() : super.price();
	}
	
	public static void discover( int cell ) {
		CellEmitter.get( cell ).start( Speck.factory( Speck.DISCOVER ), 0.1f, 4 );
	}
}
