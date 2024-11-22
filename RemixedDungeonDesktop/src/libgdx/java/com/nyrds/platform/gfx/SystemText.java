package com.nyrds.platform.gfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.utils.GLog;

public class SystemText extends Text {
	static FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/pixel_font.ttf"));
	FreeTypeFontGenerator.FreeTypeFontParameter fontParameters = new FreeTypeFontGenerator.FreeTypeFontParameter();

	BitmapFont font;

	public SystemText(float baseLine){
		super(0,0,0,0);
		fontParameters.size = (int) baseLine;
		font = generator.generateFont(fontParameters);
		GLog.debug("SystemText");
	}

	public SystemText(final String text, float size, boolean multiline) {
		super(0,0,0,0);
		fontParameters.size = (int) size;
		font = generator.generateFont(fontParameters);
		text(text);
		GLog.debug("SystemText: %s", text);
	}

	public static void updateFontScale() {
	}

	@Override
	public void destroy() {
	}

	@Override
	public void kill() {
	}


	public void measure() {
		GlyphLayout layout = new GlyphLayout(font, text);
		width = layout.width;
		height = layout.height;
		GLog.debug("SystemText: %s -> %3.0fx%3.0f", text, width, height);
	}


	@Override
	public float baseLine() {
		return height();
	}

	@Override
	public int lines() {
		return 0;
	}

	public static void invalidate() {
	}
}
