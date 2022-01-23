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

import com.nyrds.pixeldungeon.windows.IPlaceable;
import com.nyrds.platform.compatibility.RectF;
import com.nyrds.util.ModError;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.glwrap.Quad;
import com.watabou.pixeldungeon.utils.Utils;

import java.nio.FloatBuffer;

public class Image extends Visual implements IPlaceable {

	public SmartTexture texture;
	protected RectF frame;

	public boolean flipHorizontal;
	public boolean flipVertical;
	
	protected float[] vertices;
	private final FloatBuffer verticesBuffer;
	
	protected boolean dirty;


	public Image() {
		super( 0, 0, 0, 0 );
		
		vertices = new float[16];
		verticesBuffer = Quad.create();
	}
	
	public Image( Image src ) {
		this();
		copy( src );
	}
	
	public Image( Object tx ) {
		this();
		texture( tx );
	}
	
	public Image( Object tx, int left, int top, int width, int height ) {
		this( tx );
		frame( texture.uvRect( left,  top,  left + width, top + height ) );
	}

	public Image(Object tx, int cellSize, int index) {
		this(tx);

		TextureFilm film = new TextureFilm(tx, cellSize, cellSize);

		RectF frame = film.get(index);
		if (frame == null) {
			throw new ModError(Utils.format("bad index %d for image %s", index, String.valueOf(TextureCache.getKey(TextureCache.get(tx)))));
		}
		frame(frame);
	}

	public void texture( Object tx ) {
		texture = tx instanceof SmartTexture ? (SmartTexture)tx : TextureCache.get( tx );
		frame( new RectF( 0, 0, 1, 1 ) );
	}
	
	public void frame( RectF frame ) {
		this.frame = frame;
		
		width = frame.width() * texture.width;
		height = frame.height() * texture.height;
		
		updateFrame();
		updateVertices();
	}
	
	public void frame( int left, int top, int width, int height ) {
		frame( texture.uvRect( left, top, left + width, top + height ) );
	}
	
	public RectF frame() {
		return new RectF( frame );
	}

	public void copy( Image other ) {
		texture = other.texture;
		frame = new RectF( other.frame );
		
		width = other.width;
		height = other.height;
		
		updateFrame();
		updateVertices();
	}
	
	protected void updateFrame() {
		
		if (flipHorizontal) {
			vertices[2]		= frame.right;
			vertices[6]		= frame.left;
			vertices[10]	= frame.left;
			vertices[14]	= frame.right;
		} else {
			vertices[2]		= frame.left;
			vertices[6]		= frame.right;
			vertices[10]	= frame.right;
			vertices[14]	= frame.left;
		}
		
		if (flipVertical) {
			vertices[3]		= frame.bottom;
			vertices[7]		= frame.bottom;
			vertices[11]	= frame.top;
			vertices[15]	= frame.top;
		} else {
			vertices[3]		= frame.top;
			vertices[7]		= frame.top;
			vertices[11]	= frame.bottom;
			vertices[15]	= frame.bottom;
		}
		
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
	
	public void updateVerticesBuffer() {
		if (dirty) {
			verticesBuffer.position( 0 );
			verticesBuffer.put( vertices );
			dirty = false;
		}
	}
	
	@Override
	public void draw() {
		
		super.draw();

		NoosaScript script = NoosaScript.get();

		texture.bind();
		
		script.camera( camera() );
		
		script.uModel.valueM4( matrix );
		script.lighting( 
			rm, gm, bm, am, 
			ra, ga, ba, aa );
		
		updateVerticesBuffer();
		
		script.drawQuad( verticesBuffer );	
	}

	public float bottom() {
		return y + height();
	}

	public FloatBuffer getVerticesBuffer() {
		return verticesBuffer;
	}
}
