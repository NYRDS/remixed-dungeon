/*
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

package com.watabou.noosa;

import com.nyrds.platform.compatibility.RectF;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class TextureFilm {

	private final int texWidth;
	private final int texHeight;
	
	protected HashMap<Integer,RectF> frames = new HashMap<>();
	
	public TextureFilm( Object tx ) {
		
		SmartTexture texture = TextureCache.get( tx );
		
		texWidth = texture.width;
		texHeight = texture.height;
	}
	
	public TextureFilm( SmartTexture texture, int width ) {
		this( texture, width, texture.height );
	}

	public TextureFilm( Object tx, int width, int height ) {
		
		SmartTexture texture = TextureCache.get( tx );
		
		texWidth = texture.width;
		texHeight = texture.height;
		
		float uw = (float)width / texWidth;
		float vh = (float)height / texHeight;
		int cols = texWidth / width;
		int rows = texHeight / height;
		
		for (int i=0; i < rows; i++) {
			for (int j=0; j < cols; j++) {
				RectF rect = new RectF( j * uw, i * vh, (j+1) * uw, (i+1) * vh );
				add( i * cols + j, rect );
			}
		}
	}

	public int size() {
		return frames.size();
	}

	public void add( int id, RectF rect ) {
		frames.put( id, rect );
	}

	@Nullable
	public RectF get( int id ) {
		return frames.get( id );
	}
	
	public float width( RectF frame ) {
		return frame.width() * texWidth;
	}
	
	public float height( RectF frame ) {
		return frame.height() * texHeight;
	}
}