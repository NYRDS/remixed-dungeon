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
import com.watabou.glwrap.Matrix;
import com.watabou.glwrap.Quad;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.PointF;

import java.nio.FloatBuffer;

public class BitmapText extends Text {

	protected Font font;

	protected float[] vertices = new float[16];
	protected FloatBuffer quads;
	
	public int realLength;
	
	protected static final char INVALID_CHAR = ' ';
	
	public BitmapText( Font font ) {
		this(Utils.EMPTY_STRING, font );
	}
	
	public BitmapText( String text, Font font ) {
		super( 0, 0, 0, 0 );
		this.text(text);
		this.font = font;
	}
	
	@Override
	public void destroy() {
		text = Utils.EMPTY_STRING;
		font = null;
		vertices = null;
		quads = null;
		super.destroy();
	}
	
	@Override
	protected void updateMatrix() {
		// "origin" field is ignored
		Matrix.setIdentity( matrix );
		Matrix.translate( matrix, getX(), getY());
		Matrix.scale( matrix, scale.x, scale.y );
		Matrix.rotate( matrix, angle );
	}
	
	@Override
	public void draw() {
		super.draw();
		NoosaScript script = NoosaScript.get();
		
		font.texture.bind();

		clean();

		script.camera( camera() );
		
		script.uModel.valueM4( matrix );
		script.lighting( 
			rm, gm, bm, am, 
			ra, ga, ba, aa );
		
		script.drawQuadSet( quads, realLength );
	}
	
	protected void updateVertices() {
		
		setWidth(0);
		setHeight(0);
		
		quads = Quad.createSet( text.length() );
		realLength = 0;
		
		int length = text.length();
		for (int i=0; i < length; i++) {
			RectF rect = font.get( text.charAt( i ) );
	
			if (rect == null) {
				rect = font.get(INVALID_CHAR);
			}
			float w = font.width( rect );
			float h = font.height( rect );
			
			float sx = 0;
			float sy = 0;
			
			PointF sp = font.glyphShift.get(text.charAt( i ));
			
			if(sp != null) {
				sx = sp.x;
				sy = sp.y;
			}

            vertices[0] 	= width + sx;
			vertices[1] 	= sy;
			
			vertices[2]		= rect.left;
			vertices[3]		= rect.top;

            vertices[4] 	= width + w + sx;
			vertices[5] 	= sy;
			
			vertices[6]		= rect.right;
			vertices[7]		= rect.top;

            vertices[8] 	= width + w + sx;
			vertices[9] 	= h + sy;
		
			vertices[10]	= rect.right;
			vertices[11]	= rect.bottom;

            vertices[12]	= width + sx;
			vertices[13]	= h + sy;
			
			vertices[14]	= rect.left;
			vertices[15]	= rect.bottom;
			
			quads.put( vertices );
			realLength++;

            setWidth(width + w + font.tracking);
			if (h + sy > height) {
				setHeight(h + sy);
			}
		}
		
		if (length > 0) {
            setWidth(width - font.tracking);
		}
	}

	protected void measure() {
		updateVertices();
	}
	
	public float baseLine() {
		return font.baseLine * scale.y;
	}

	@Override
	public int lines() {
		return 1;
	}

	protected float glyphShiftY(char c) {
		PointF shift = font.glyphShift.get(c);
		
		if(shift != null) {
			return shift.y;
		}
		
		return 0;
	}
}
