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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.microedition.khronos.opengles.GL10;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
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
import com.watabou.utils.PointF;

public class PixelScene extends Scene {

	// Minimum virtual display size for portrait orientation
	public static final float MIN_WIDTH_P = 128;
	public static final float MIN_HEIGHT_P = 224;

	// Minimum virtual display size for landscape orientation
	public static final float MIN_WIDTH_L = 224;
	public static final float MIN_HEIGHT_L = 160;

	public static float defaultZoom = 0;
	public static float minZoom;
	public static float maxZoom;

	public static Camera uiCamera;

	public static BitmapText.Font font1x;
	public static BitmapText.Font font;

	
	
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

		while ((Game.width() / defaultZoom < minWidth || Game.height()
				/ defaultZoom < minHeight)
				&& defaultZoom > 1) {

			defaultZoom--;
		}

		if (PixelDungeon.scaleUp()) {
			while (Game.width() / (defaultZoom + 1) >= minWidth
					&& Game.height() / (defaultZoom + 1) >= minHeight) {

				defaultZoom++;
			}
		}

		minZoom = 1;
		maxZoom = defaultZoom * 2;

		GLog.i("%d %d %f", Game.width(), Game.height(), defaultZoom);

		Camera.reset(new PixelCamera(defaultZoom));

		float uiZoom = defaultZoom;
		uiCamera = Camera.createFullscreen(uiZoom);
		Camera.add(uiCamera);

		createFonts();
	}

	public static Bitmap createBitmapFromFont(AssetManager assets, String file,
			String chars, int size, HashMap<Object, RectF> metrics,
			HashMap<Object, PointF> shifts, float fontHeight) {

		int padX = 2;
		int padY = 1;
		// load the font and setup paint instance for drawing
		Typeface tf = Typeface.createFromAsset(assets, file); // Create the
																// Typeface from
																// Font File
		Paint paint = new Paint(); // Create Android Paint Instance
		paint.setAntiAlias(true); // Enable Anti Alias
		paint.setTextSize(size); // Set Text Size
		paint.setColor(0xffffffff); // Set ARGB (White, Opaque)
		paint.setTypeface(tf); // Set Typeface

		// get font metrics
		Paint.FontMetrics fm = paint.getFontMetrics(); // Get Font Metrics
		
		fontHeight = (float) Math.ceil(Math.abs(fm.bottom)
				+ Math.abs(fm.top)); // Calculate
		// Ascent
		float fontDescent = (float) Math.ceil(Math.abs(fm.descent)); // Save
																		// Font
		// Descent

		// determine the width of each character (including unknown character)
		// also determine the maximum character width
		char[] s = new char[1]; // Create Character Array
		float[] w = new float[1]; // Working Width Value

		HashMap<Object, Float> charWidths = new HashMap<Object, Float>();

		float charWidthMax = 0f;

		for (int i = 0; i < chars.length(); i++) { // FOR Each Character
			s[0] = chars.charAt(i); // Set Character
			paint.getTextWidths(s, 0, 1, w); // Get Character Bounds

			charWidths.put(s[0], w[0]);

			if (charWidthMax < w[0]) {
				charWidthMax = w[0];
			}
		}

		// find the maximum size, validate, and setup cell sizes
		int cellWidth = (int) charWidthMax + (2 * padX); // Set Cell Width

		int totalArea = (int) (cellWidth * fontHeight * chars.length());

		int textureSizeX = 64;
		int textureSizeY = 64;

		while (totalArea > textureSizeX * textureSizeY) {
			if (textureSizeY < textureSizeX) {
				textureSizeY *= 2;
			} else {
				textureSizeX *= 2;
			}
		}

		//GLog.w("creating %d x %d texture", textureSizeX, textureSizeY);

		// create an empty bitmap (alpha only)
		Bitmap bitmap = Bitmap.createBitmap(textureSizeX, textureSizeY,
				Bitmap.Config.ARGB_8888); // Create Bitmap
		Canvas canvas = new Canvas(bitmap); // Create Canvas for Rendering to
											// Bitmap
		bitmap.eraseColor(0x00000000); // Set Transparent Background (ARGB)

		// render each of the characters to the canvas (ie. build the font map)
		float yShift = (fontHeight - 1) - fontDescent - padY;

		float x = 0; // Set Start Position (X)
		float y = yShift; // Set Start
							// Position (Y)
		for (int i = 0; i < chars.length(); i++) { // FOR Each Character
			s[0] = chars.charAt(i); // Set Character

			float thisCellWidth = charWidths.get(s[0]) + padX * 2;

			Rect bounds = new Rect();
			
			if(i == 0){ //special case for space
				bounds.right = size / 3;
				//GLog.i("making space");
			} else {
				paint.getTextBounds(s, 0, 1, bounds);
			}
			metrics.put(s[0], new RectF((x + bounds.left) / textureSizeX,
					(y + bounds.top) / textureSizeY, (x + bounds.right + 1)
							/ textureSizeX, (y + bounds.bottom + 2) / textureSizeY));

			shifts.put(s[0], new PointF(bounds.left, size+bounds.top));

			//GLog.w("rendering char %d at %3.0f,%3.0f", (int) s[0], x, y);
			canvas.drawText(s, 0, 1, x, y, paint); // Draw Character
			x += thisCellWidth; // Move to Next Character
			if ((x + thisCellWidth - padX) > textureSizeX) {
				x = 0; // Set X for New Row
				y += fontHeight; // Move Down a Row
			}
		}

		return bitmap;

	}

	private static Font createFont(int size) {
		float fontHeight = 0;
		
		HashMap<Object, RectF> metrics = new HashMap<Object, RectF>();
		HashMap<Object, PointF> shifts = new HashMap<Object, PointF>();
		Bitmap bitmap = createBitmapFromFont(Game.instance().getAssets(),
				"Roboto-Regular.ttf", BitmapText.Font.ALL_CHARS, size,
				metrics, shifts, fontHeight);

		File storageDir = Game.instance().getExternalFilesDir(null);
		File file = new File(storageDir, "tst.png");

		OutputStream fOut = null;
		try {
			fOut = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		boolean res = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut); // saving
																				// the
																				// Bitmap
																				// to
																				// a
																				// file
																				// compressed
																				// as
																				// a
																				// JPEG
																				// with
																				// 85%
																				// compression
																				// rate

		try {
			if (res) {
				fOut.flush();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			fOut.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // do not forget to close the stream

		font = Font.createEmptyFont(bitmap);

		Iterator<Entry<Object, RectF>> it = metrics.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Object, RectF> pair = it.next();
			font.add(pair.getKey(), pair.getValue());
		}

		Iterator<Entry<Object, PointF>> it1 = shifts.entrySet().iterator();
		while (it1.hasNext()) {
			Entry<Object, PointF> pair = it1.next();
			
			PointF p = pair.getValue();
			
			font.addGlyphShift((Character) pair.getKey(),p);
		}
		
		font.lineHeight = fontHeight;
		font.baseLine = size;

		return font;
	}
	
	private void createFonts() {
		if (font1x == null) {
			// 3x5 (6)
			font1x = Font.colorMarked(BitmapCache.get(Assets.FONTS1X),
					0x00000000, BitmapText.Font.LATIN_FULL);
			font1x.baseLine = 6;
			font1x.tracking = -1;
		}
	}

	@Override
	public void destroy() {
		super.destroy();
		Touchscreen.event.removeAll();
	}
	
	private static HashMap<Integer, BitmapText.Font> fontCache = new HashMap<Integer, BitmapText.Font>();
	public static float scale;

	public static void chooseFont(float size) {
		
		int fontSize = (int) (size*2) + PixelDungeon.fontScale() - 2;
		
		if(fontSize < 8) {
			fontSize = 8;
		}
		
		font = fontCache.get(fontSize);
		
		if(font == null) {
			font = createFont(fontSize);
			fontCache.put(fontSize, font);
		}
		

		scale = 0.5f;
	}

	public static BitmapText createText(float size) {
		return createText(null, size);
	}

	public static BitmapText createText(String text, float size) {

		chooseFont(size);

		BitmapText result = new BitmapText(text, font);
		result.scale.set(scale);

		return result;
	}

	public static BitmapTextMultiline createMultiline(float size) {
		return createMultiline(null, size);
	}

	public static BitmapTextMultiline createMultiline(String text, float size) {

		chooseFont(size);

		BitmapTextMultiline result = new BitmapTextMultiline(text, font);
		result.scale.set(scale);

		return result;
	}

	public static float align(Camera camera, float pos) {
		return ((int) (pos * camera.zoom)) / camera.zoom;
	}

	// This one should be used for UI elements
	public static float align(float pos) {
		return ((int) (pos * defaultZoom)) / defaultZoom;
	}

	public static void align(Visual v) {
		Camera c = v.camera();
		v.x = align(c, v.x);
		v.y = align(c, v.y);
	}

	public static boolean noFade = false;

	protected void fadeIn() {
		if (noFade) {
			noFade = false;
		} else {
			fadeIn(0xFF000000, false);
		}
	}

	protected void fadeIn(int color, boolean light) {
		add(new Fader(color, light));
	}

	public static void showBadge(Badges.Badge badge) {
		BadgeBanner banner = BadgeBanner.show(badge.image);
		banner.camera = uiCamera;
		banner.x = align(banner.camera,
				(banner.camera.width - banner.width) / 2);
		banner.y = align(banner.camera,
				(banner.camera.height - banner.height) / 3);
		Game.scene().add(banner);
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

		public Fader(int color, boolean light) {
			super(uiCamera.width, uiCamera.height, color);

			this.light = light;

			camera = uiCamera;

			alpha(1f);
			time = FADE_TIME;
		}

		@Override
		public void update() {

			super.update();

			if ((time -= Game.elapsed) <= 0) {
				alpha(0f);
				parent.remove(this);
			} else {
				alpha(time / FADE_TIME);
			}
		}

		@Override
		public void draw() {
			if (light) {
				GLES20.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE);
				super.draw();
				GLES20.glBlendFunc(GL10.GL_SRC_ALPHA,
						GL10.GL_ONE_MINUS_SRC_ALPHA);
			} else {
				super.draw();
			}
		}
	}

	private static class PixelCamera extends Camera {

		public PixelCamera(float zoom) {
			super(
					(int) (Game.width() - Math.ceil(Game.width() / zoom) * zoom) / 2,
					(int) (Game.height() - Math.ceil(Game.height() / zoom)
							* zoom) / 2, (int) Math.ceil(Game.width() / zoom),
					(int) Math.ceil(Game.height() / zoom), zoom);
		}

		@Override
		protected void updateMatrix() {
			float sx = align(this, scroll.x + shakeX);
			float sy = align(this, scroll.y + shakeY);

			matrix[0] = +zoom * invW2;
			matrix[5] = -zoom * invH2;

			matrix[12] = -1 + x * invW2 - sx * matrix[0];
			matrix[13] = +1 - y * invH2 - sy * matrix[5];

		}
	}
}
