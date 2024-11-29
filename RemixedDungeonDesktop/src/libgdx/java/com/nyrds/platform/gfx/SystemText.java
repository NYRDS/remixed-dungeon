package com.nyrds.platform.gfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.nyrds.pixeldungeon.game.GamePreferences;
import com.watabou.glwrap.Matrix;
import com.watabou.noosa.Text;

import java.util.ArrayList;

public class SystemText extends Text {
	private static FreeTypeFontGenerator generator;
	private final FreeTypeFontParameter fontParameters;
	private final BitmapFont font;
	private final GlyphLayout glyphLayout;
	private ArrayList<String> lines;

	private static float fontScale = Float.NaN;

	private static SystemTextPseudoBatch batch = new SystemTextPseudoBatch();
	private static float oversample = 4;

	static {
		generator = new FreeTypeFontGenerator(Gdx.files.internal("../assets/fonts/pixel_font.ttf"));
	}

	public SystemText(float baseLine) {
		super(0, 0, 0, 0);
		fontParameters = getFontParameters(baseLine);

		font = generator.generateFont(fontParameters);
		glyphLayout = new GlyphLayout();
	}

	private FreeTypeFontParameter getFontParameters(float baseLine) {
		if (fontScale != fontScale) {
			updateFontScale();
		}

		final FreeTypeFontParameter fontParameters;
		fontParameters = new FreeTypeFontParameter();
		fontParameters.size = (int) (baseLine * oversample * fontScale);
		fontParameters.borderColor = Color.BLACK;
		fontParameters.borderWidth = oversample;
		fontParameters.flip = true;
		fontParameters.genMipMaps = true;
		fontParameters.magFilter = Texture.TextureFilter.Linear;
		fontParameters.minFilter = Texture.TextureFilter.MipMapLinearLinear;
		fontParameters.spaceX = 0;
		fontParameters.spaceY = 0;
		return fontParameters;
	}

	public SystemText(final String text, float size, boolean multiline) {
		super(0, 0, 0, 0);
		fontParameters = getFontParameters(size);

		font = generator.generateFont(fontParameters);
		glyphLayout = new GlyphLayout();
		this.text(text);
		if (multiline) {
			lines = new ArrayList<>();
			splitLines(text);
		}
	}

	public static void updateFontScale() {
		float scale = 0.5f + 0.01f * GamePreferences.fontScale();

		scale *= 1.2f;

		if (scale < 0.1f) {
			fontScale = 0.1f;
			return;
		}
		if (scale > 4) {
			fontScale = 4f;
			return;
		}

		fontScale = scale;
	}

	private void splitLines(String text) {
		lines.clear();
		String[] split = text.split("\n");
		for (String line : split) {
			lines.add(line);
		}
	}

	@Override
	public void destroy() {
		font.dispose();

	}

	@Override
	public void kill() {
		destroy();
	}

	@Override
	protected void updateMatrix() {
		if (dirtyMatrix) {

			Matrix.setIdentity(matrix);
			Matrix.translate(matrix, x, y);
			if (angle != 0) {
				Matrix.rotate(matrix, angle);
			}

			Matrix.scale(matrix, scale.x/oversample, scale.y/oversample);

			dirtyMatrix = false;
		}
	}

	@Override
	public void draw() {
		updateMatrix();

		SystemTextPseudoBatch.textBeingRendered = this;

		if (lines != null && !lines.isEmpty()) {
			float y = 0;
			for (String line : lines) {
				glyphLayout.setText(font, line);
				font.draw(batch, glyphLayout, 0,  y);
				y -= glyphLayout.height;
			}
		} else {
			glyphLayout.setText(font, text);
			font.draw(batch, glyphLayout, 0, 0);
		}
	}

	@Override
	public void setWidth(float width) {
		super.setWidth(width/oversample);
	}

	@Override
	public void setHeight(float height) {
		super.setHeight(height/oversample);
	}

	@Override
	public void measure() {
		if (lines != null && !lines.isEmpty()) {
			float totalHeight = 0;
			for (String line : lines) {
				glyphLayout.setText(font, line);
				totalHeight += glyphLayout.height;
			}
			setHeight(totalHeight);
			for (String line : lines) {
				glyphLayout.setText(font, line);
				if (glyphLayout.width > width) {
					setWidth(glyphLayout.width);

				}
			}
		} else {
			glyphLayout.setText(font, text);
			setWidth(glyphLayout.width);
			setHeight(glyphLayout.height);
		}
	}

	@Override
	public float baseLine() {
		return height();
	}

	@Override
	public int lines() {
		if (lines != null) {
			return lines.size();
		}
		return 1;
	}

	public static void invalidate() {
		if (generator != null) {
			generator.dispose();
			generator = new FreeTypeFontGenerator(Gdx.files.internal("../assets/fonts/pixel_font.ttf"));
		}
	}
}