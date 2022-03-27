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
import com.watabou.glwrap.Quad;
import com.watabou.utils.PointF;

public class BitmapTextMultiline extends BitmapText {

	protected float spaceSize;
	
	
	public BitmapTextMultiline( String text, Font font ) {
		super( text, font );
		spaceSize = font.width( font.get( ' ' ) );
	}
	
	@Override
	protected void updateVertices() {

		quads = Quad.createSet( text.length() );
		realLength = 0;
		
		// This object controls lines breaking
		SymbolWriter writer = new SymbolWriter();
		
		// Word size
		PointF metrics = new PointF();
		
		String paragraphs[] = PARAGRAPH.split( text );
		
		// Current character (used in masking)
		int pos = 0;

		for (String paragraph : paragraphs) {

			String[] words = WORD.split(paragraph);

			for (String word : words) {

				if (word.length() == 0) {
					// This case is possible when there are
					// several spaces coming along
					continue;
				}

				getWordMetrics(word, metrics);
				writer.addSymbol(metrics.x, metrics.y);

				int length = word.length();
				float shift = 0;    // Position in pixels relative to the beginning of the word

				for (int k = 0; k < length; k++) {
					RectF rect = font.get(word.charAt(k));

					//Corrigido
					if (rect == null) {
						rect = font.get(INVALID_CHAR);
					}

					float sx = 0;
					float sy = 0;

					PointF sp = font.glyphShift.get(word.charAt(k));

					if (sp != null) {
						sx = sp.x;
						sy = sp.y;
					}

					float w = font.width(rect);
					float h = font.height(rect);

					if (mask == null || mask[pos]) {
						vertices[0] = writer.x + shift + sx;
						vertices[1] = writer.y + sy;

						vertices[2] = rect.left;
						vertices[3] = rect.top;

						vertices[4] = writer.x + shift + w + sx;
						vertices[5] = writer.y + sy;

						vertices[6] = rect.right;
						vertices[7] = rect.top;

						vertices[8] = writer.x + shift + w + sx;
						vertices[9] = writer.y + h + sy;

						vertices[10] = rect.right;
						vertices[11] = rect.bottom;

						vertices[12] = writer.x + shift + sx;
						vertices[13] = writer.y + h + sy;

						vertices[14] = rect.left;
						vertices[15] = rect.bottom;

						quads.put(vertices);
						realLength++;
					}

					shift += w + font.tracking;

					pos++;
				}

				writer.addSpace(spaceSize);
			}

			writer.newLine(0, font.lineHeight);
		}

		setWidth(writer.width);
		setHeight(writer.height);
	}
	
	private void getWordMetrics( String word, PointF metrics ) {
		
		float w = 0;
		float h = 0;
		
		int length = word.length();
		for (int i=0; i < length; i++) {
			
			RectF rect = font.get( word.charAt( i ) );
			//Corrigido
			if (rect == null) {
				rect = font.get(INVALID_CHAR);
			}
			w += font.width( rect ) + (w > 0 ? font.tracking : 0);
			
			h = Math.max( h, font.height( rect ) + glyphShiftY(word.charAt( i )));
		}
		
		metrics.set( w, h );
	}


	public int lines(){
        return (int) (height /font.lineHeight);
	}

	private class SymbolWriter {
		
		public float width = 0;
		public float height = 0;
		
		public float lineWidth = 0;
		public float lineHeight = 0;
		
		public float x = 0;
		public float y = 0;
		
		public void addSymbol( float w, float h ) {
			if (lineWidth > 0 && lineWidth + font.tracking + w > getMaxWidth() / scale.x) {
				newLine( w, h );
			} else {
				
				x = lineWidth;
				
				lineWidth += (lineWidth > 0 ? font.tracking : 0) + w;
				if (h > lineHeight) {
					lineHeight = h;
				}
			}
		}
		
		public void addSpace( float w ) {
			if (lineWidth > 0 && lineWidth + font.tracking + w > getMaxWidth() / scale.x) {
				newLine( 0, 0 );
			} else {
				
				x = lineWidth;
				lineWidth += (lineWidth > 0 ? font.tracking : 0) + w;
			}
		}
		
		public void newLine( float w, float h ) {
			
			height += lineHeight;
			if (width < lineWidth) {
				width = lineWidth;
			}
			
			lineWidth = w;
			lineHeight = h;
			
			x = 0;
			y = height;
		}
	}
}
