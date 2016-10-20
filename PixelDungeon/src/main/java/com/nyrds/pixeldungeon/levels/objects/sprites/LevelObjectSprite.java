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
package com.nyrds.pixeldungeon.levels.objects.sprites;

import com.nyrds.pixeldungeon.levels.objects.LevelObject;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.utils.PointF;

public class LevelObjectSprite extends MovieClip {

	private static final int SIZE = 16;

	private static TextureFilm frames;

	private int pos = -1;

	public LevelObjectSprite() {

		if (frames == null) {
			frames = new TextureFilm( texture, SIZE, SIZE );
		}

		origin.set(SIZE/2, SIZE/2);
	}

	public void reset(LevelObject object ) {
		revive();
		texture (object.texture());
		reset( object.image() );
		alpha( 1f );
		
		pos = object.getPos();
		PointF p = DungeonTilemap.tileToWorld(object.getPos());
		x = p.x;
		y = p.y;
	}
	
	public void reset( int image ) {
		frame( frames.get( image ) );
	}
	
	@Override
	public void update() {
		super.update();
		setVisible(pos == -1 || Dungeon.visible[pos]);
	}

}
