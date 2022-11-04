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

import android.opengl.GLES20;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.windows.WndHelper;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.game.RemixedDungeon;
import com.nyrds.platform.gfx.SystemText;
import com.nyrds.platform.input.Touchscreen;
import com.nyrds.platform.storage.Preferences;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.nyrds.util.ModdingMode;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Font;
import com.watabou.noosa.Scene;
import com.watabou.noosa.Text;
import com.watabou.noosa.Visual;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.effects.BadgeBanner;
import com.watabou.pixeldungeon.utils.Utils;

import javax.microedition.khronos.opengles.GL10;

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

	public static Font font1x;
	public static Font font25x;

	public static Font font;

	private static void viewportSizeChanged() {
		float minWidth, minHeight;

		if (RemixedDungeon.landscape()) {
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

			defaultZoom-=0.01;
		}

		WndHelper.update(Game.width() / defaultZoom, Game.height() / defaultZoom);

		minZoom = 1;
		maxZoom = defaultZoom * 2;

		Camera.reset(new PixelCamera(defaultZoom));

		float uiZoom = (float) Preferences.INSTANCE.getDouble(Preferences.KEY_UI_ZOOM, defaultZoom);
		uiCamera = Camera.createFullscreen(uiZoom);
		Camera.add(uiCamera);
	}

	@Override
	public void create() {

		super.create();

		viewportSizeChanged();
		createFonts();
	}

	static private void createFonts() {
		// 3x5 (6)
		font1x = Font.colorMarked(TextureCache.get(Assets.FONTS1X),
				0x00000000, Font.LATIN_FULL);
		font1x.baseLine = 6;
		font1x.tracking = -1;

		// 7x12 (15)
		font25x = Font.colorMarked(
			TextureCache.get( Assets.FONTS25X ), 17, 0x00000000, Font.ALL_CHARS);
		font25x.baseLine = 13;
		font25x.tracking = -1;
	}

	@Override
	public void destroy() {
		super.destroy();
		Touchscreen.event.removeAll();
	}

	public static float scale;

	public static Font chooseFont(float size) {
		scale = size / 14.f;

		scale /= 1.8;

		if(Game.smallResScreen()) {
			scale /= 2;
		}

		font = font25x;

		return font;
	}

	public static Text createText(float size) {
		return createText(Utils.EMPTY_STRING, size);
	}

	public static Text createText(String text, float size) {

		if(!ModdingMode.getClassicTextRenderingMode()) {
			return new SystemText(text, size, false);
		}

		Text result = Text.create(text, chooseFont(size));
		result.setScale(scale);

		return result;
	}

	public static Text createMultiline() {
		return createMultiline(Utils.EMPTY_STRING, GuiProperties.regularFontSize());
	}

	public static Text createMultiline(float size) {
		return createMultiline(Utils.EMPTY_STRING, size);
	}

	public static Text createMultiline(final String text, float size) {

		if(!ModdingMode.getClassicTextRenderingMode()) {
			return new SystemText(text, size, true);
		}

		Text result = Text.createMultiline(text, chooseFont(size));
		result.setScale(scale);

		return result;
	}

	public static Text createMultiline(int id, float size) {
		return createMultiline(StringsManager.getVar(id), size);
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
		v.setX(align(c, v.getX()));
		v.setY(align(c, v.getY()));
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
		if(!(GameLoop.scene() instanceof GameScene)) {
			return;
		}
		
		if (uiCamera != null && !Game.isPaused()) {
			BadgeBanner banner = BadgeBanner.show(badge.image);
			banner.camera = uiCamera;
            banner.setX(align(banner.camera,
					(banner.camera.width - banner.width) / 2));
			banner.setY(align(banner.camera,
					(banner.camera.height - banner.height) / 3));
			GameLoop.addToScene(banner);
		}
	}

	public static Font font() {
		return font;
	}

	public static void font(Font font) {
		PixelScene.font = font;
	}

	private static class Fader extends ColorBlock {

		private static float FADE_TIME = 1f;

		private boolean light;

		private float time;

		Fader(int color, boolean light) {
			super(uiCamera.width, uiCamera.height, color);

			this.light = light;

			camera = uiCamera;

			alpha(1f);
			time = FADE_TIME;
		}

		@Override
		public void update() {

			super.update();

			if ((time -= GameLoop.elapsed) <= 0) {
				alpha(0f);
				getParent().remove(this);
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

		PixelCamera(float zoom) {
			super(
					(int) (Game.width() - Math.ceil(Game.width() / zoom) * zoom) / 2,
					(int) (Game.height() - Math.ceil(Game.height() / zoom)* zoom) / 2,
					(int) Math.ceil(Game.width() / zoom),
					(int) Math.ceil(Game.height() / zoom),
					zoom);
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
