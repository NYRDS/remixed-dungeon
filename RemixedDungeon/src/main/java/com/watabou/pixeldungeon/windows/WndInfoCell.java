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
package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.levels.objects.LevelObject;
import com.nyrds.pixeldungeon.levels.objects.sprites.LevelObjectSprite;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.CharsList;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.nyrds.util.Util;
import com.watabou.noosa.Image;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.Utils;

public class WndInfoCell extends Window {

	private static final int WIDTH = 120;
	
	public WndInfoCell( int cell ) {
		
		super();
		Level level = Dungeon.level;

		LevelObject obj = level.getTopLevelObject(cell);

		int tile = level.getTileType(cell);
		
		IconTitle titlebar = new IconTitle();
		if (tile == Terrain.WATER) {
			Image water = new Image( level.getWaterTex() );
			water.frame( 0, 0, DungeonTilemap.SIZE, DungeonTilemap.SIZE );
			titlebar.icon( water );
		} else {
			titlebar.icon(GameScene.getTile( cell ) );
		}

		String title = level.tileNameByCell( cell );
		final boolean haveVisibleObject = obj != null && !obj.secret();

		if(haveVisibleObject) {
			title = obj.name();
		}

		titlebar.label( title );
		titlebar.setRect( 0, 0, WIDTH, 0 );
		add( titlebar );

		float yPos = titlebar.bottom();

		Text info = PixelScene.createMultiline(GuiProperties.regularFontSize());
		add( info );

		StringBuilder desc = new StringBuilder( level.tileDescByCell( cell ) );

		final char newLine = '\n';

		if(haveVisibleObject) {
			LevelObjectSprite sprite = new LevelObjectSprite();
			sprite.reset(obj);
			float xs = obj.getSpriteXS();
			float ys = obj.getSpriteYS();
			sprite.setPos(-(xs - DungeonTilemap.SIZE) / 2, -(ys-DungeonTilemap.SIZE) / 2);
			sprite.setScaleXY(DungeonTilemap.SIZE/xs,DungeonTilemap.SIZE/ys);
			sprite.setIsometricShift(false);
			add(sprite);

			titlebar.icon(sprite);
			desc = new StringBuilder(obj.desc());
		}

		for (Blob blob:level.blobs.values()) {
			if (blob.cur[cell] > 0 && blob.tileDesc() != null) {
				if (desc.length() > 0) {
					desc.append( newLine );
				}
				desc.append( blob.tileDesc() );
			}
		}


		if(Util.isDebug()) {
			desc.append(Utils.format("\n\ncell: %d x: %d y: %d", cell, level.cellX(cell), level.cellY(cell)));
			desc.append(Utils.format("\ntile: %d", tile));
			desc.append(Utils.format("\npassable: %b", level.passable[cell]));
			desc.append(Utils.format("\navoid: %b", level.avoid[cell]));
			desc.append(Utils.format("\nsolid: %b", level.solid[cell]));
			desc.append(Utils.format("\nflammable: %b", level.flammable[cell]));
			if(obj!=null) {
				desc.append(Utils.format("\nlo: %s layer: %d", obj.getEntityKind(), obj.getLayer()));
				desc.append(Utils.format("\nnonPassable: %b", obj.nonPassable(CharsList.DUMMY)));
				desc.append(Utils.format("\nsecret: %b", obj.secret()));
			}
		}

        info.text( desc.length() > 0 ? desc.toString() : StringsManager.getVar(R.string.WndInfoCell_Nothing));
		info.maxWidth(WIDTH);
		info.setX(titlebar.left());
		info.setY(yPos + GAP);

		resize( WIDTH, (int)(info.bottom()) );
	}
}
