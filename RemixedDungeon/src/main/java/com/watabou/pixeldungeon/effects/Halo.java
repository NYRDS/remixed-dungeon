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
package com.watabou.pixeldungeon.effects;

import com.nyrds.platform.gfx.BitmapData;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Image;

public class Halo extends Image {

	private static final Object CACHE_KEY = Halo.class;

	protected static final int RADIUS	= 64;

	protected float radius = RADIUS;
	protected float brightness = 1;

	public Halo() {
		if (!TextureCache.contains( CACHE_KEY )) {
			BitmapData bmp = BitmapData.createBitmap( RADIUS * 2, RADIUS * 2 );
			bmp.makeHalo(RADIUS, 0x88FFFFFF, 0xFFFFFFFF );
			TextureCache.add( CACHE_KEY, new SmartTexture( bmp ) );
		}

		texture( CACHE_KEY );

		origin.set( RADIUS );
	}

	public Halo( float radius, int color, float brightness ) {

		this();

		hardlight( color );
		alpha( this.brightness = brightness );
		radius( radius );
	}

	public Halo point( float x, float y ) {
		this.x = x - RADIUS;
		this.y = y - RADIUS;
		return this;
	}

	public void radius( float value ) {
		scale.set(  (this.radius = value) / RADIUS );
	}
}
