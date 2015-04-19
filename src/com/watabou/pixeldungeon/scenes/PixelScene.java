/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
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
package com.watabou.pixeldungeon.scenes;

import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;

import com.watabou.input.Touchscreen;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.BitmapText.Font;
import com.watabou.noosa.BitmapTextMultiline;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Scene;
import com.watabou.noosa.Visual;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.effects.BadgeBanner;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.BitmapCache;

public class PixelScene extends Scene {
	
	// Minimum virtual display size for portrait orientation
	public static final float MIN_WIDTH_P		= 128;
	public static final float MIN_HEIGHT_P		= 224;
	
	// Minimum virtual display size for landscape orientation
	public static final float MIN_WIDTH_L		= 224;
	public static final float MIN_HEIGHT_L		= 160;
	
	public static float defaultZoom = 0;
	public static float minZoom;
	public static float maxZoom;
	
	public static Camera uiCamera;
	
	public static BitmapText.Font font1x;
	public static BitmapText.Font font25x;
	
	@Override
	public void create() {
		
		super.create();

		float minWidth, minHeight;
		if (PixelDungeon.landscape()) {
			minWidth = MIN_WIDTH_L;
			minHeight = MIN_HEIGHT_L;
		} else {
			minWidth = MIN_WIDTH_P;
			minHeight = MIN_HEIGHT_P;
		}
		
		defaultZoom = 20;
		
		while ((
			Game.width() / defaultZoom < minWidth || 
			Game.height() / defaultZoom < minHeight
			) && defaultZoom > 1) {
			
			defaultZoom--;
		}
			
		if (PixelDungeon.scaleUp()) {
			while (
				Game.width() / (defaultZoom + 1) >= minWidth && 
				Game.height() / (defaultZoom + 1) >= minHeight) {
				
				defaultZoom++;
			}	
		}
		
		minZoom = 1;
		maxZoom = defaultZoom * 2;	
		
		GLog.i("%d %d %f", Game.width(), Game.height(), defaultZoom);
		
		Camera.reset( new PixelCamera( defaultZoom ) );
		
		float uiZoom = defaultZoom;
		uiCamera = Camera.createFullscreen( uiZoom );
		Camera.add( uiCamera );
		
		createFonts();
	}
	
	private void createFonts(){
		if (font1x == null) {
			// 3x5 (6)
			font1x = Font.colorMarked( 
				BitmapCache.get( Assets.FONTS1X ), 0x00000000, BitmapText.Font.LATIN_FULL );
			font1x.baseLine = 6;
			font1x.tracking = -1;
			
			// 7x12 (15)
			font25x = Font.colorMarked( 
				BitmapCache.get( Assets.FONTS25X ), 17, 0x00000000, BitmapText.Font.ALL_CHARS);
			font25x.baseLine = 13;
			font25x.tracking = -1;
		}
	}
	
	@Override
	public void destroy() {
		super.destroy();
		Touchscreen.event.removeAll();
	}
	
	private static BitmapText.Font font;
	public static float scale;
	
	public static void chooseFont( float size ) {
		font = font25x;
		float userScale = Math.min(PixelDungeon.fontScale() * 0.1f, 0.1f);
		scale = 0.05f * size * (1 + userScale);
	}
	
	public static BitmapText createText( float size ) {
		return createText( null, size );
	}
	
	public static BitmapText createText( String text, float size ) {
		
		chooseFont( size );
		
		BitmapText result = new BitmapText( text, font );
		result.scale.set( scale );
		
		return result;
	}
	
	public static BitmapTextMultiline createMultiline( float size ) {
		return createMultiline( null, size );
	}
	
	public static BitmapTextMultiline createMultiline( String text, float size ) {
		
		chooseFont( size );
		
		BitmapTextMultiline result = new BitmapTextMultiline( text, font );
		result.scale.set( scale );
		
		return result;
	}
	
	public static float align( Camera camera, float pos ) {
		return ((int)(pos * camera.zoom)) / camera.zoom;
	}
	
	// This one should be used for UI elements
	public static float align( float pos ) {
		return ((int)(pos * defaultZoom)) / defaultZoom;
	}
	
	public static void align( Visual v ) {
		Camera c = v.camera();
		v.x = align( c, v.x );
		v.y = align( c, v.y );
	}
	
	public static boolean noFade = false;
	protected void fadeIn() {
		if (noFade) {
			noFade = false;
		} else {
			fadeIn( 0xFF000000, false );
		}
	}
	
	protected void fadeIn( int color, boolean light ) {
		add( new Fader( color, light ) );
	}
	
	public static void showBadge( Badges.Badge badge ) {
		BadgeBanner banner = BadgeBanner.show( badge.image );
		banner.camera = uiCamera;
		banner.x = align( banner.camera, (banner.camera.width - banner.width) / 2 );
		banner.y = align( banner.camera, (banner.camera.height - banner.height) / 3 );
		Game.scene().add( banner );
	}
	
	public static BitmapText.Font font() {
		return font;
	}

	public static void font(BitmapText.Font font) {
		PixelScene.font = font;
	}

	protected static class Fader extends ColorBlock {
		
		private static float FADE_TIME = 1f;
		
		private boolean light;
		
		private float time;
		
		public Fader( int color, boolean light ) {
			super( uiCamera.width, uiCamera.height, color );
			
			this.light = light;
			
			camera = uiCamera;
			
			alpha( 1f );
			time = FADE_TIME;
		}
		
		@Override
		public void update() {
			
			super.update();
			
			if ((time -= Game.elapsed) <= 0) {
				alpha( 0f );
				parent.remove( this );
			} else {
				alpha( time / FADE_TIME );
			}
		}
		
		@Override
		public void draw() {
			if (light) {
				GLES20.glBlendFunc( GL10.GL_SRC_ALPHA, GL10.GL_ONE );
				super.draw();
				GLES20.glBlendFunc( GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA );
			} else {
				super.draw();
			}
		}
	}
	
	private static class PixelCamera extends Camera {
		
		public PixelCamera( float zoom ) {
			super( 
				(int)(Game.width() - Math.ceil( Game.width() / zoom ) * zoom) / 2, 
				(int)(Game.height() - Math.ceil( Game.height() / zoom ) * zoom) / 2, 
				(int)Math.ceil( Game.width() / zoom ), 
				(int)Math.ceil( Game.height() / zoom ), zoom );
		}
		
		@Override
		protected void updateMatrix() {
			float sx = align( this, scroll.x + shakeX );
			float sy = align( this, scroll.y + shakeY );
			
			matrix[0] = +zoom * invW2;
			matrix[5] = -zoom * invH2;
			
			matrix[12] = -1 + x * invW2 - sx * matrix[0];
			matrix[13] = +1 - y * invH2 - sy * matrix[5];
			
		}
	}
}
