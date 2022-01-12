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
import com.nyrds.platform.gfx.BitmapData;
import com.nyrds.platform.gl.Texture;
import com.watabou.gltextures.SmartTexture;
import com.watabou.glwrap.Quad;

import java.nio.FloatBuffer;
import java.util.Map;
import java.util.WeakHashMap;

public class SystemTextLine extends Visual {

	private SmartTexture texture;
	private RectF frame;
	
	private final float[] vertices;
	private final FloatBuffer verticesBuffer;
	
	private boolean dirty;

	static final Map<String, SystemTextLine> linesCache = new WeakHashMap<>();

	public SystemTextLine() {
		super( 0, 0, 0, 0 );
		
		vertices = new float[16];
		verticesBuffer = Quad.create();
	}
		
	public SystemTextLine(BitmapData bitmap) {
		this();

		texture = new SmartTexture(bitmap, Texture.LINEAR, Texture.CLAMP);

		frame( new RectF( 0, 0, 1, 1 ) );
	}

	@Override
	public void destroy() {
		super.destroy();
		if(texture != null) {
			texture.delete();
			texture = null;
		}
	}
	
	public void frame( RectF frame ) {
		this.frame = frame;
		
		width = frame.width() * texture.width;
		height = frame.height() * texture.height;
		
		updateFrame();
		updateVertices();
	}
	

	protected void updateFrame() {
		vertices[2]		= frame.left;
		vertices[6]		= frame.right;
		vertices[10]	= frame.right;
		vertices[14]	= frame.left;

		vertices[3]		= frame.top;
		vertices[7]		= frame.top;
		vertices[11]	= frame.bottom;
		vertices[15]	= frame.bottom;
		
		dirty = true;
	}
	
	protected void updateVertices() {
		
		vertices[0] 	= 0;
		vertices[1] 	= 0;
		
		vertices[4] 	= width;
		vertices[5] 	= 0;
		
		vertices[8] 	= width;
		vertices[9] 	= height;
		
		vertices[12]	= 0;
		vertices[13]	= height;
		
		dirty = true;
	}
	
	@Override
	public void draw() {
		if(texture == null) { // used as empty rows
			return;
		}
		
		super.draw();

		NoosaScript script = NoosaScript.get();

		texture.bind();

		script.camera( camera() );
		
		script.uModel.valueM4( matrix );
		script.lighting( 
			rm, gm, bm, am, 
			ra, ga, ba, aa );
		
		if (dirty) {
			verticesBuffer.position( 0 );
			verticesBuffer.put( vertices );
			dirty = false;
		}
		script.drawQuad( verticesBuffer );	
	}
}
