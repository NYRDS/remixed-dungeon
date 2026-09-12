package com.nyrds.platform.gfx;

import com.nyrds.pixeldungeon.game.GamePreferences;
import com.watabou.glwrap.Matrix;
import com.watabou.noosa.SystemTextBase;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import lombok.val;
import org.jetbrains.annotations.NotNull;

// headless: real TTF metrics via AWT (works headless), drawing is dropped.
// keeps the desktop oversample layout math so window geometry stays close.
public class SystemText extends SystemTextBase {

	private static final float oversample = 4;
	private static final Map<Integer, Font> pixelFontCache = new HashMap<>();
	private static final Map<Integer, Font> fallbackFontCache = new HashMap<>();
	private static Font pixelFontBase;
	private static Font fallbackFontBase;
	private static BufferedImage measureBitmap;
	private static java.awt.Graphics2D measureGraphics;

	static {
		invalidate();
	}

	private final float baseLine;
	private boolean multiline = false;
	private boolean useFallbackFont = false;

	public SystemText(float baseLine) {
		super(0, 0, 0, 0);
		this.baseLine = baseLine;
		this.originalText = "";
	}

	public SystemText(final String text, float size, boolean multiline) {
		this(size);
		this.multiline = multiline;
		text(text);
	}

	public static void updateFontScale() {
		float scale = 0.5f + 0.01f * (GamePreferences.fontScale() + 9);
		scale *= 1.2f;
		fontScale = Math.max(0.1f, Math.min(4f, scale));
	}

	private static void ensureBaseFonts() {
		if (pixelFontBase != null) {
			return;
		}
		pixelFontBase = loadFont("fonts/pixel_font.ttf");
		fallbackFontBase = loadFont("fonts/LXGWWenKaiScreen.ttf");
	}

	private static Font loadFont(String resName) {
		try (InputStream stream = com.nyrds.util.ModdingMode.getInputStream(resName)) {
			Font font = Font.createFont(Font.TRUETYPE_FONT, stream);
			return font;
		} catch (Exception e) {
			return new Font(Font.DIALOG, Font.PLAIN, 12);
		}
	}

	private static java.awt.Graphics2D measureGraphics() {
		if (measureGraphics == null) {
			measureBitmap = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
			measureGraphics = measureBitmap.createGraphics();
			measureGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		}
		return measureGraphics;
	}

	private Font awtFont() {
		ensureBaseFonts();
		if (Float.isNaN(fontScale)) {
			updateFontScale();
		}
		int size = Math.max(1, (int) (baseLine * oversample * fontScale));
		Map<Integer, Font> cache = useFallbackFont ? fallbackFontCache : pixelFontCache;
		Font base = useFallbackFont ? fallbackFontBase : pixelFontBase;
		synchronized (cache) {
			Font f = cache.get(size);
			if (f == null) {
				f = base.deriveFont(Font.PLAIN, size);
				cache.put(size, f);
			}
			return f;
		}
	}

	private FontMetrics metrics() {
		return measureGraphics().getFontMetrics(awtFont());
	}

	@Override
	protected float measureTextWidth(String text) {
		if (text.isEmpty()) {
			return 0;
		}
		return metrics().stringWidth(text);
	}

	private void parseMarkupAndWrap() {
		if (multiline && maxWidth == Integer.MAX_VALUE) {
			return;
		}

		textLines.clear();

		var allSegments = parseMarkupToSegments(originalText);
		hasMarkup = !allSegments.isEmpty() && allSegments.size() > 1;

		var wrappedLines = wrapText(allSegments, maxWidth);

		for (var line : wrappedLines) {
			textLines.add(new ArrayList<>(line));
		}
	}

	@Override
	protected void updateMatrix() {
		if (dirtyMatrix) {
			Matrix.setIdentity(matrix);
			Matrix.translate(matrix, x, y);
			if (angle != 0) {
				Matrix.rotate(matrix, angle);
			}
			Matrix.scale(matrix, scale.x / oversample, scale.y / oversample);
			dirtyMatrix = false;
		}
	}

	@Override
	public void draw() {
		measure();
		// headless: nothing to draw
	}

	@Override
	public void setWidth(float width) {
		super.setWidth(width / oversample);
	}

	@Override
	public void setHeight(float height) {
		super.setHeight(height / oversample);
	}

	@Override
	protected void measure() {
		if (dirty) {
			parseMarkupAndWrap();

			float totalHeight = 0;
			float maxLineWidth = 0;

			FontMetrics fm = metrics();

			for (val line : textLines) {
				float currentLineWidth = 0;
				for (val segment : line) {
					currentLineWidth += measureTextWidth(segment.text);
				}
				if (currentLineWidth > maxLineWidth) {
					maxLineWidth = currentLineWidth;
				}
			}
			totalHeight = textLines.size() * fm.getHeight();

			setWidth(maxLineWidth);
			setHeight(totalHeight);
			dirty = false;
		}

		if (height < minHeight) {
			setHeight(minHeight);
		}
	}

	@Override
	public float baseLine() {
		FontMetrics fm = metrics();
		return fm.getHeight() / oversample;
	}

	@Override
	public int lines() {
		return textLines.size();
	}

	public static void invalidate() {
		pixelFontBase = null;
		fallbackFontBase = null;
		pixelFontCache.clear();
		fallbackFontCache.clear();
		measureGraphics = null;
		measureBitmap = null;
	}

	@Override
	public void text(@NotNull String str) {
		super.text(str);
		this.dirty = true;
		String plainText = extractPlainText(str);
		this.useFallbackFont = !GamePreferences.classicFont() || !stringsCovered(plainText);
	}

	private static boolean stringsCovered(String text) {
		// headless: AWT logical fonts cover ~everything through fallback
		return true;
	}
}
