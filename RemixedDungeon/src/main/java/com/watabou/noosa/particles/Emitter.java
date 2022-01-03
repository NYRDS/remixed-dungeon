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

package com.watabou.noosa.particles;

import android.opengl.GLES20;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Group;
import com.watabou.noosa.Visual;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

import javax.microedition.khronos.opengles.GL10;

public class Emitter extends Group {

	protected boolean lightMode = false;

	public float x;
	public float y;
	public float width;
	public float height;

	protected Visual target;

	protected float interval;
	protected int quantity;
	
	public boolean on = false;
	
	public boolean autoKill = true;

	protected int count;
	protected float time;
	
	protected Factory factory;
	
	public void pos( float x, float y ) {
		pos( x, y, 0, 0 );
	}
	
	public void pos( PointF p ) {
		pos( p.x, p.y, 0, 0 );
	}
	
	public void pos( float x, float y, float width, float height ) {
		this.x = x;
		this.y = y + (isometricShift ? Gizmo.isometricShift() : 0);
		this.width = width;
		this.height = height;
		
		target = null;
	}
	
	public void pos( Visual target ) {
		this.target = target;
		x = target.x;
		y = target.y;
	}

	public void pos( Visual target, float ix, float iy, float width, float height ) {
		this.target = target;
		x = target.x;
		y = target.y;
	}
	
	public void burst( Factory factory, int quantity ) {
		start( factory, 0, quantity );
	}
	
	public void pour( Factory factory, float interval ) {
		start( factory, interval, 0 );
	}

	public void start(@NotNull Factory factory, float interval, int quantity ) {
		
		this.factory = factory;
		this.lightMode = factory.lightMode();
		
		this.interval = interval;
		this.quantity = quantity;
		
		count = 0;
		time = Random.Float( interval );
		
		on = true;
	}
	
	@Override
	public void update() {
		
		if (on) {
			time += GameLoop.elapsed;
			while (time > interval) {
				time -= interval;
				emit( count );
				if (quantity > 0 && ++count >= quantity) {
					on = false;
					break;
				}
			}
		} else if (autoKill && countLiving() == 0) {
			kill();
		}
		
		super.update();
	}
	
	protected void emit( int index ) {
		if (target == null) {
			factory.emit( 
				this,
				index,
				x + Random.Float( width ),
				y + Random.Float( height ) );
		} else {
			factory.emit( 
				this,
				index,
				target.x + target.visualOffsetX() + Random.Float( target.visualWidth() ),
				target.y + target.visualOffsetY() + Random.Float( target.visualHeight() ) );
		}
	}
	
	@Override
	public void draw() {
		if (lightMode) {
			GLES20.glBlendFunc( GL10.GL_SRC_ALPHA, GL10.GL_ONE );
			super.draw();
			GLES20.glBlendFunc( GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA );
		} else {
			super.draw();
		}
	}

	public void setIsometricShift(boolean isometricShift) {
		if(Dungeon.isIsometricMode()) {
			this.isometricShift = isometricShift;
		}
	}

	abstract public static class Factory {
		
		abstract public void emit( Emitter emitter, int index, float x, float y );
		
		public boolean lightMode() {
			return false;
		}
	}
}
