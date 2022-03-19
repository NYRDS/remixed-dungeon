/*
 * Pixel Dungeon
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
package com.watabou.pixeldungeon.effects;

import android.opengl.GLES20;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.watabou.gltextures.Gradient;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.NoosaScript;
import com.watabou.noosa.Visual;
import com.watabou.utils.SystemTime;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

@LuaInterface
public class Flare extends Visual {
	
	private float duration = 0;
	private float lifespan;
	
	private boolean lightMode = true;
	
	private final SmartTexture texture;
	
	private final FloatBuffer vertices;
	private final ShortBuffer indices;
	
	private final int nRays;

	private boolean permanent = false;

	@LuaInterface
	public Flare( int nRays, float radius ) {
		
		super( 0, 0, 0, 0 );

		texture = TextureCache.getOrCreate(Flare.class, () ->
		{
			int[] gradient = {0xFFFFFFFF, 0x00FFFFFF};
			return new Gradient(gradient);
		});

		this.nRays = nRays;
		
		setAngle(45);
		angularSpeed = 180;
		
		vertices = ByteBuffer.
			allocateDirect( (nRays * 2 + 1) * 4 * (Float.SIZE / 8) ).
			order( ByteOrder.nativeOrder() ).
			asFloatBuffer();
		
		indices = ByteBuffer.
			allocateDirect( nRays * 3 * Short.SIZE / 8 ).
			order( ByteOrder.nativeOrder() ).
			asShortBuffer();
		
		float[] v = new float[4];
		
		v[0] = 0;
		v[1] = 0;
		v[2] = 0.25f;
		v[3] = 0;
		vertices.put( v );
		
		v[2] = 0.75f;
		v[3] = 0;
		
		for (int i=0; i < nRays; i++) {
			
			float a = i * 3.1415926f * 2 / nRays;
			v[0] = (float) (Math.cos( a ) * radius);
			v[1] = (float) (Math.sin( a ) * radius);
			vertices.put( v );
			
			a += 3.1415926f * 2 / nRays / 2;
			v[0] = (float) (Math.cos( a ) * radius);
			v[1] = (float) (Math.sin( a ) * radius);
			vertices.put( v );
			
			indices.put( (short)0 );
			indices.put( (short)(1 + i * 2) );
			indices.put( (short)(2 + i * 2) );
		}
		
		indices.position( 0 );
	}

	public Flare permanent() {
		permanent = true;
		return this;
	}

	@LuaInterface
	public Flare color( int color, boolean lightMode ) {
		this.lightMode = lightMode;
		hardlight( color );
		
		return this;
	}


	@LuaInterface
	public Flare showOnTop( Visual visual, float duration ) {
		point( visual.center() );
		visual.getParent().add( this );

		lifespan = this.duration = duration;

		return this;
	}

	@LuaInterface
	public Flare show( Visual visual, float duration ) {
		point( visual.center() );
		visual.getParent().addToBack( this );

		lifespan = this.duration = duration;
		
		return this;
	}

	@LuaInterface
	static public void attach(int nRays, float radius, int color, boolean lightMode, Visual sprite, float duration) {
		new Flare(nRays, radius).color(color, lightMode).show(sprite, duration);
	}

	@Override
	public void update() {
		super.update();
		
		if (duration > 0) {
			if (lifespan > 0) {

				float p;

				if(! permanent) {
					lifespan -= GameLoop.elapsed;
					p = 1 - lifespan / duration;    // 0 -> 1
				} else {
					p = (float) Math.pow(Math.sin(SystemTime.now()/1000.f),2) + 0.1f;
				}

				p =  p < 0.25f ? p * 4 : (1 - p) * 1.333f;
				setScale( p );
				alpha( p );
				
			} else {
				killAndErase();
			}
		}
	}
	
	@Override
	public void draw() {
		
		super.draw();
		
		if (lightMode) {
			GLES20.glBlendFunc( GL10.GL_SRC_ALPHA, GL10.GL_ONE );
			drawRays();
			GLES20.glBlendFunc( GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA );
		} else {
			drawRays();
		}
	}
	
	private void drawRays() {
		
		NoosaScript script = NoosaScript.get();
		
		texture.bind();
		
		script.uModel.valueM4( matrix );
		script.lighting( 
			rm, gm, bm, am, 
			ra, ga, ba, aa );
		
		script.camera( camera );
		script.drawElements( vertices, indices, nRays * 3 );
	}
}
