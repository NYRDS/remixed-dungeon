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
import com.nyrds.platform.gl.Texture;

public class SkinnedBlock extends Image {

	protected float scaleX;
	protected float scaleY;

	protected float offsetX;
	protected float offsetY;
	
	public SkinnedBlock( float width, float height, Object tx ) {
		super( tx );
		
		texture.wrap( Texture.REPEAT, Texture.REPEAT );
		
		size(width, height);
	}
	
	@Override
	public void frame( RectF frame ) {
		scaleX = 1;
		scaleY = 1;
		
		offsetX = 0;
		offsetY = 0;
		
		super.frame( new RectF( 0, 0, 1, 1 ) );
	}
	
	@Override
	protected void updateFrame() {
		
		float tw = 1f / texture.width;
		float th = 1f / texture.height;
		
		float u0 = wrapTexCoord(offsetX * tw);
		float v0 = wrapTexCoord(offsetY * th);

        float u1 = u0 + width * tw / scaleX;
		float v1 = v0 + height * th / scaleY;
		
		vertices[2]		= u0;
		vertices[3]		= v0;
		
		vertices[6]		= u1;
		vertices[7]		= v0;
		
		vertices[10]	= u1;
		vertices[11]	= v1;
		
		vertices[14]	= u0;
		vertices[15]	= v1;
		
		dirty = true;
	}

	private float wrapTexCoord(float u0) {
		if(u0 > 1) {
			u0 = (float) (u0 - Math.floor(u0));
		}

		if(u0 < -1) {
			u0 = (float) (u0 - Math.ceil(u0));
		}
		return u0;
	}

	public void offsetTo( float x, float y ) {
		offsetX = x;
		offsetY = y;
		updateFrame();
	}
	
	public void offset( float x, float y ) {
		offsetX += x;
		offsetY += y;
		updateFrame();
	}
	
	public float offsetX() {
		return offsetX;
	}
	
	public float offsetY() {
		return offsetY;
	}
	
	public void scale( float x, float y ) {
		scaleX = x;
		scaleY = y;
		updateFrame();
	}
	
	public void size( float w, float h ) {
		this.setWidth(w);
		this.setHeight(h);
		updateFrame();
		updateVertices();
	}
}
