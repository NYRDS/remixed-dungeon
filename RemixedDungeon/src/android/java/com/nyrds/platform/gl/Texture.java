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

package com.nyrds.platform.gl;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class Texture {

	public static final int NEAREST	= GLES20.GL_NEAREST;
	public static final int LINEAR	= GLES20.GL_LINEAR;

	public static final int REPEAT	= GLES20.GL_REPEAT;
	public static final int MIRROR	= GLES20.GL_MIRRORED_REPEAT;
	public static final int CLAMP	= GLES20.GL_CLAMP_TO_EDGE;
	
	protected int id;
	
	public Texture() {}
	
	public static void activate( int index ) {
		GLES20.glActiveTexture( GLES20.GL_TEXTURE0 + index );
	}

	private void ensureTexture() {
		if(id==0) {
			int[] ids = new int[1];
			GLES20.glGenTextures( 1, ids, 0 );
			id = ids[0];
		}
	}

	protected void _bind() {
		ensureTexture();
		GLES20.glBindTexture( GLES20.GL_TEXTURE_2D, id );
	}

	public void bind() {
		_bind();
	}

	public void filter( int minMode, int maxMode ) {
		_bind();
		GLES20.glTexParameterf( GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, minMode );
		GLES20.glTexParameterf( GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, maxMode );
	}
	
	public void wrap( int s, int t ) {
		_bind();
		GLES20.glTexParameterf( GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, s );
		GLES20.glTexParameterf( GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, t );
	}
	
	public void delete() {
		int[] ids = {id};
		GLES20.glDeleteTextures( 1, ids, 0 );
		id = 0;
		//Log.i("texture",Utils.format("deleting %d", id));
	}
	
	public void bitmap( Bitmap bitmap ) {
		_bind();
		GLUtils.texImage2D( GLES20.GL_TEXTURE_2D, 0, bitmap, 0 );
	}
	
	public void pixels( int w, int h, int[] pixels ) {
	
		_bind();
		
		IntBuffer imageBuffer = ByteBuffer.
			allocateDirect( w * h * 4 ).
			order( ByteOrder.nativeOrder() ).
			asIntBuffer();
		imageBuffer.put( pixels );
		imageBuffer.position( 0 );
		
		GLES20.glTexImage2D(
			GLES20.GL_TEXTURE_2D, 
			0, 
			GLES20.GL_RGBA, 
			w, 
			h, 
			0, 
			GLES20.GL_RGBA, 
			GLES20.GL_UNSIGNED_BYTE, 
			imageBuffer );
	}
	
	// If getConfig returns null (unsupported format?), GLUtils.texImage2D works
	// incorrectly. In this case we need to load pixels manually
	public void handMade( Bitmap bitmap, boolean recode ) {

		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		
		int[] pixels = new int[w * h]; 
		bitmap.getPixels( pixels, 0, w, 0, 0, w, h );

		// recode - components reordering is needed
		if (recode) {
			for (int i=0; i < pixels.length; i++) {
				int color = pixels[i];		
				int ag = color & 0xFF00FF00;
				int r = (color >> 16) & 0xFF;
				int b = color & 0xFF;
				pixels[i] = ag | (b << 16) | r;
			}
		}
		
		pixels( w, h, pixels );
	}
}
