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

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.windows.IPlaceable;
import com.watabou.glwrap.Matrix;
import com.watabou.utils.GameMath;
import com.watabou.utils.Point;
import com.watabou.utils.PointF;

public class Visual extends Gizmo implements IPlaceable{

	public float x;
	public float y;
	public float width;
	public float height;

	public PointF scale;
	public PointF origin;

	public boolean dirtyMatrix = true;
	
	protected float[] matrix;
	
	public float rm;
	public float gm;
	public float bm;
	public float am;
	public float ra;
	public float ga;
	public float ba;
	public float aa;
	
	public PointF speed;
	public PointF acc;
	
	public float angle;
	public float angularSpeed;
	
	public Visual( float x, float y, float width, float height ) {
		this.setX(x);
		this.setY(y);
		this.setWidth(width);
		this.setHeight(height);
		
		Scale(new PointF( 1, 1 ));
		setOrigin(new PointF());
		
		matrix = new float[16];
		
		resetColor();
		
		speed = new PointF();
		acc = new PointF();
	}
	
	@Override
	public void update() {
		updateMotion();
	}
	
	@Override
	public void draw() {
		updateMatrix();
	}
	
	protected void updateMatrix() {
		if (dirtyMatrix) {

			Matrix.setIdentity(matrix);
			Matrix.translate(matrix, getX(), getY() + visualOffsetY());
			Matrix.translate(matrix, origin.x, origin.y);
			if (angle != 0) {
				Matrix.rotate(matrix, angle);
			}
			if (scale.x != 1 || scale.y != 1) {
				Matrix.scale(matrix, scale.x, scale.y);
			}
			Matrix.translate(matrix, -origin.x, -origin.y);
			dirtyMatrix = false;
		}
	}
	
	public PointF point() {
		return new PointF(getX(), getY());
	}
	
	public PointF point( PointF p ) {
		setX(p.x);
		setY(p.y);
		return p;
	}
	
	public Point point( Point p ) {
		setX(p.x);
		setY(p.y);
		return p;
	}
	
//	public PointF center() {
//		return new PointF( x + width / 2 + visualOffsetX(), y + height / 2 + visualOffsetY());
//	}


	public PointF center() {
		return new PointF( getX() + width / 2, getY() + height / 2);
	}

	public float width() {
		return width * scale.x;
	}
	
	public float height() {
		return height * scale.y;
	}
	
	protected void updateMotion() {
		float elapsed = GameLoop.elapsed;
		
		float d = (GameMath.speed( speed.x, acc.x ) - speed.x) / 2;
		speed.x += d;
		x +=  speed.x * elapsed;
		speed.x += d;
		
		d = (GameMath.speed( speed.y, acc.y ) - speed.y) / 2;
		speed.y += d;
		y += speed.y * elapsed;
		speed.y += d;
		
		angle += angularSpeed * elapsed;

		if(speed.x != 0 || speed.y != 0 || angularSpeed != 0) {
			dirtyMatrix = true;
		}
	}
	
	public void alpha( float value ) {
		am = value;
		aa = 0;
	}
	
	public float alpha() {
		return am + aa;
	}
	
	public void invert() {
		rm = gm = bm = -1f;
		ra = ga = ba = +1f;
	}
	
	public void lightness( float value ) {
		if (value < 0.5f) {	
			rm = gm = bm = value * 2f;
			ra = ga = ba = 0;
		} else {
			rm = gm = bm = 2f - value * 2f;
			ra = ga = ba = value * 2f - 1f;
		}
	}
	
	public void brightness( float value ) {
		rm = gm = bm = value;
	}
	
	public void tint( float r, float g, float b, float strength ) {
		rm = gm = bm = 1f - strength;
		ra = r * strength;
		ga = g * strength;
		ba = b * strength;
	}
	
	public void tint( int color, float strength ) {
		rm = gm = bm = 1f - strength;
		ra = ((color >> 16) & 0xFF) / 255f * strength;
		ga = ((color >> 8) & 0xFF) / 255f * strength;
		ba = (color & 0xFF) / 255f * strength;
	}
	
	public void color( float r, float g, float b ) {
		rm = gm = bm = 0;
		ra = r;
		ga = g;
		ba = b;
	}
	
	public void color( int color ) {
		color( ((color >> 16) & 0xFF) / 255f, ((color >> 8) & 0xFF) / 255f, (color & 0xFF) / 255f );
	}
	
	public void hardlight( float r, float g, float b ) {
		ra = ga = ba = 0;
		rm = r;
		gm = g;
		bm = b;
	}
	
	public void hardlight( int color ) {
		hardlight( (color >> 16) / 255f, ((color >> 8) & 0xFF) / 255f, (color & 0xFF) / 255f );
	}
	
	public void resetColor() {
		rm = gm = bm = am = 1;
		ra = ga = ba = aa = 0;
	}
	
	public boolean overlapsPoint( float x, float y ) {
		return x >= this.getX() && x < this.getX() + width * scale.x && y >= this.getY() && y < this.getY() + height * scale.y;
	}
	
	public boolean overlapsScreenPoint( int x, int y ) {
		Camera c = camera();
		if (c != null) {
			PointF p = c.screenToCamera( x, y );
			return overlapsPoint( p.x, p.y );
		} else {
			return false;
		}
	}
	
	// true if its bounding box intersects its camera's bounds
	public boolean isVisible() {
		Camera c = camera();
		if(c==null) {
			return false;
		}

		float cx = c.scroll.x;
		float cy = c.scroll.y;
		float w = width();
		float h = height();
		return getX() + w >= cx && getY() + h >= cy && getX() < cx + c.width && getY() < cy + c.height;
	}

	public void setPos(float x, float y){
		this.setX(x);
		this.setY(y);
	}

	@LuaInterface
	public void incX(float x) {
		setX(getX() + x);
	}

	@LuaInterface
	public void incY(float y) {
		setY(getY() + y);
	}

	@Override
	public float getX() {
		return x;
	}

	@Override
	public float getY() {
		return y;
	}

	public void Scale(PointF scale) {
		setScale(scale);
	}

	public float bottom() {
		return getY() + height();
	}

    public float visualWidth() {
		return width;
	}

	public float visualHeight() {
		return height;
	}

	public float visualOffsetX() {
		return 0;
	}


	public float visualOffsetY() {
		if(isometricShift) {
			return isometricModeShift;
		}
		return 0;
	}

	public void setX(float x) {
		this.x = x;
		dirtyMatrix = true;
	}

	public void setY(float y) {
		this.y = y;
		dirtyMatrix = true;
	}

	public void setWidth(float width) {
		this.width = width;
		dirtyMatrix = true;
	}

	public void setHeight(float height) {
		this.height = height;
		dirtyMatrix = true;
	}

	public void setScale(float s) {
		this.scale.set(s);
		dirtyMatrix = true;
	}

	public void setScaleX(float x) {
		scale.x = x;
		dirtyMatrix = true;
	}

	public void setScaleY(float y) {
		scale.y = y;
		dirtyMatrix = true;
	}


	public void setScale(float x, float y) {
		scale.x = x;
		scale.y = y;
		dirtyMatrix = true;
	}

	public void setScale(PointF scale) {
		this.scale = scale;
		dirtyMatrix = true;
	}

	public void setOrigin(PointF origin) {
		this.origin = origin;
		dirtyMatrix = true;
	}

	public void setOrigin(float s) {
		this.origin.set(s);
		dirtyMatrix = true;
	}


	public void setOrigin(float x, float y) {
		this.origin.set(x,y);
		dirtyMatrix = true;
	}}
