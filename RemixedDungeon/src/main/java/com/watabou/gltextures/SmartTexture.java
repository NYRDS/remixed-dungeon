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

package com.watabou.gltextures;

import com.nyrds.platform.compatibility.RectF;
import com.nyrds.platform.gfx.BitmapData;
import com.nyrds.platform.gl.Texture;

import org.jetbrains.annotations.NotNull;

public class SmartTexture extends Texture {

	public int width;
	public int height;

	public int fModeMin;
	public int fModeMax;

	public int wModeH;
	public int wModeV;
	boolean loaded;

	public BitmapData bitmap;

	public SmartTexture() {
		super();
	}

	public SmartTexture(@NotNull BitmapData bitmap ) {
		this( bitmap, NEAREST, CLAMP );
	}

	public SmartTexture(@NotNull BitmapData bitmap, int filtering, int wrapping ) {
		super();
		
		bitmap( bitmap );
		filter( filtering, filtering );
		wrap( wrapping, wrapping );
	}

	@Override
	public void filter(int minMode, int maxMode) {
		fModeMin = minMode;
		fModeMax = maxMode;
	}
	
	@Override
	public void wrap( int s, int t ) {
		wModeH = s;
		wModeV = t;
	}

	@Override
	public void bitmap( BitmapData bitmap ) {
		handMade(bitmap, true );


	public void bind() {
		_bind();
		if(!loaded) {
			super.filter(fModeMin,fModeMax);
			super.wrap(wModeH,wModeV);

			if(bitmap!=null) {
				handMade(bitmap);
			}

			loaded = true;
		}
	}

	public void bitmap( Bitmap bitmap) {
		this.bitmap = bitmap;
		width = bitmap.getWidth();
		height = bitmap.getHeight();
	}


	@Override
	public void delete() {
		super.delete();
		//bitmap = null;
		loaded = false;
	}
	
	public RectF uvRect( int left, int top, int right, int bottom ) {
		return new RectF(
			(float)left / width,
			(float)top	/ height,
			(float)right / width,
			(float)bottom / height );
	}
}
