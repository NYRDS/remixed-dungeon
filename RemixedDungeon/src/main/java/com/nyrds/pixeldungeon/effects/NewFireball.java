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
package com.nyrds.pixeldungeon.effects;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.compatibility.RectF;
import com.nyrds.platform.gl.Gl;
import com.nyrds.platform.gl.Texture;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.Assets;
import com.watabou.utils.ColorMath;
import com.watabou.utils.Random;

public class NewFireball extends Component {

	private static final RectF BLIGHT = new RectF( 0, 0, 0.25f, 1 );
	private static final RectF FLIGHT = new RectF( 0.25f, 0, 0.5f, 1 );
	private static final RectF FLAME1 = new RectF( 0.50f, 0, 0.75f, 1 );
	private static final RectF FLAME2 = new RectF( 0.75f, 0, 1.00f, 1 );
	
	private static final int COLOR = 0xFF0000;
	
	private Image bLight;
	private Image fLight;
	private Emitter emitter;
	private Group sparks;

	@Override
	protected void createChildren() {
		
		sparks = new Group();
		add( sparks );
		
		bLight = new Image( Assets.FIREBALL );
		bLight.frame( BLIGHT );
        bLight.setOrigin( bLight.width / 2 );
		bLight.angularSpeed = -120;
		add( bLight );
		
		emitter = new Emitter();
		emitter.pour( new Emitter.Factory() {
			@Override
			public void emit(Emitter emitter, int index, float x, float y) {
				Flame p = (Flame)emitter.recycle( Flame.class );
				p.reset();
                p.setX(x - p.width / 2);
				p.setY(y - p.height / 2);
			}
		}, 0.1f );
		add( emitter );
		
		fLight = new Image( Assets.FIREBALL );
		fLight.frame( FLIGHT );
        fLight.setOrigin( fLight.width / 2 );
		fLight.angularSpeed = 360;
		add( fLight );
		
		bLight.texture.filter( Texture.LINEAR, Texture.LINEAR );
	}
	
	@Override
	protected void layout() {

        bLight.setX(x - bLight.width / 2);
		bLight.setY(y - bLight.height / 2);

		emitter.pos(
			x - bLight.width / 4,
			y - bLight.height / 4,
			bLight.width / 2,
			bLight.height / 2 );

        fLight.setX(x - fLight.width / 2);
		fLight.setY(y - fLight.height / 2);
	}
	
	@Override
	public void update() {
		
		super.update();
		
		if (Random.Float() < GameLoop.elapsed) {
			PixelParticle spark = (PixelParticle)sparks.recycle( PixelParticle.Shrinking.class );
			spark.reset( x, y, ColorMath.random( 0x000000, COLOR ), 2, Random.Float( 0.5f, 1.0f ) );
			spark.speed.set( 
				Random.Float( -70, +40 ),
				Random.Float( -60, +70 ) );
			spark.acc.set( 0, +80 );
			sparks.add( spark );
		}
	}
	
	@Override
	public void draw() {
		Gl.blendSrcAlphaOne();
		super.draw();
		Gl.blendSrcAlphaOneMinusAlpha();
	}
	
	public static class Flame extends Image {
		
		private static float LIFESPAN	= 1f;
		
		private static float SPEED	= -20f;
		private static float ACC	= -20f;
		
		private float timeLeft;
		
		public Flame() {
			
			super( Assets.FIREBALL );
			
			frame( Random.Int( 2 ) == 0 ? FLAME1 : FLAME2 );
			setOrigin( width / 2, height / 2 );
			acc.set( 0, ACC );
		}
		
		public void reset() {
			revive();
			timeLeft = LIFESPAN;
			speed.set( 0, SPEED );
		}
		
		@Override
		public void update() {
			
			super.update();
			
			if ((timeLeft -= GameLoop.elapsed) <= 0) {
				
				kill();
				
			} else {
				
				float p = timeLeft / LIFESPAN;
				setScale( p, p );
				alpha( p > 0.8f ? (1 - p) * 5f : p * 1.25f );
				
			}
		}
	}
}
