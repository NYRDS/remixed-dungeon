package com.watabou.noosa;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.text.TextPaint;

import com.nyrds.android.util.TrackedRuntimeException;
import com.watabou.glwrap.Matrix;
import com.watabou.pixeldungeon.scenes.PixelScene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SystemText extends Text {

	private String text;

	static private Map<Float, TextPaint> textPaints = new HashMap<>();
	private TextPaint textPaint;

	static private Map<Float, TextPaint> contourPaints = new HashMap<>();
	private        TextPaint             contourPaint  = new TextPaint();

	private ArrayList<SystemTextLine> lineImage = new ArrayList<>();

	private static Set<SystemText> texts = new HashSet<>();

	private static Typeface tf;
	private static float    oversample;

	private boolean needWidth = false;

	public SystemText(float baseLine) {
		this("", baseLine, false);
	}

	public SystemText(String text, float baseLine, boolean multiline) {
		super(0, 0, 0, 0);

		if (tf == null) {
			if (Game.smallResScreen()) {
				tf = Typeface.create((String) null, Typeface.BOLD);
				oversample = 1;
			} else {
				tf = Typeface.create((String) null, Typeface.NORMAL);
				oversample = 4;
			}
		}

		baseLine *= PixelScene.computeFontScale();

		needWidth = multiline;

		if (baseLine == 0) {
			throw new TrackedRuntimeException("zero sized font!!!");
		}

		float textSize = baseLine * oversample;
		if (!textPaints.containsKey(textSize)) {
			TextPaint tx = new TextPaint();

			tx.setTextSize(textSize);
			tx.setStyle(Paint.Style.FILL);
			tx.setAntiAlias(true);

			tx.setColor(Color.WHITE);

			tx.setTypeface(tf);

			TextPaint cp = new TextPaint();
			cp.set(tx);
			cp.setStyle(Paint.Style.STROKE);
			cp.setStrokeWidth(textSize / 10);
			cp.setColor(Color.BLACK);

			textPaints.put(textSize, tx);
			contourPaints.put(textSize, cp);
		}

		textPaint = textPaints.get(textSize);
		contourPaint = contourPaints.get(textSize);

		this.text(text);
		texts.add(this);
	}

	private void destroyLines() {
		for (SystemTextLine img : lineImage) {
			if (getParent() != null) {
				getParent().remove(img);
			}
			img.destroy();
		}
	}

	@Override
	public void destroy() {
		destroyLines();

		text = null;
		super.destroy();
		texts.remove(this);
	}

	@Override
	public void kill() {
		destroyLines();

		text = null;
		super.kill();
		texts.remove(this);
	}

	private ArrayList<Float> xCharPos = new ArrayList<>();

	private float fontHeight;

	private int fillLine(int startFrom) {
		int offset = startFrom;

		float xPos = 0;
		xCharPos.clear();

		final int length = text.length();
		int lastWordOffset = offset;

		for (; offset < length; ) {
			final int codepoint = text.codePointAt(offset);
			int codepointCharCount = Character.charCount(codepoint);

			xCharPos.add(xPos);

			float xDelta = symbolWidth(text.substring(offset, offset
					+ codepointCharCount));

			offset += codepointCharCount;

			if (Character.isWhitespace(codepoint)) {
				lastWordOffset = offset;
			}

			if (codepoint == 0x000A) {
				return offset;
			}

			xPos += xDelta;

			if (maxWidth != Integer.MAX_VALUE
					&& xPos > maxWidth / scale.x) {
				if (lastWordOffset != startFrom) {
					return lastWordOffset;
				} else {
					return offset - 1;
				}
			}
		}
		xCharPos.add(xPos);
		// Log.d("SystemText", Utils.format("eot"));
		return offset;
	}

	@SuppressLint("NewApi")
	private void createText() {
		if (text == null) {
			return;
		}

		if (needWidth && maxWidth == Integer.MAX_VALUE) {
			return;
		}

		if (fontHeight > 0) {
			destroyLines();
			lineImage.clear();
			width = height = 0;

			height = fontHeight/4;

			int charIndex = 0;
			int startLine = 0;

			while (startLine < text.length()) {
				int nextLine = fillLine(startLine);

				float lineWidth = 0;

				if (nextLine > 0) {
					lineWidth = xCharPos.get(xCharPos.size() - 1) + 1;
					width = Math.max(lineWidth, width);
				}

				height += fontHeight;

				if (lineWidth > 0) {

					Bitmap bitmap = Bitmap.createBitmap(
							(int) (lineWidth * oversample),
							(int) (fontHeight * oversample),
							Bitmap.Config.ARGB_4444);

					Canvas canvas = new Canvas(bitmap);

					int offset = startLine;
					int lineCounter = 0;
					for (; offset < nextLine; ) {
						final int codepoint = text.codePointAt(offset);
						int codepointCharCount = Character.charCount(codepoint);

						if (!Character.isWhitespace(codepoint)) {
							if (mask == null
									|| (charIndex < mask.length && mask[charIndex])) {

								String txt = text.substring(offset, offset + codepointCharCount);
								float x = (xCharPos.get(lineCounter) + 0.5f) * oversample;
								float y = (fontHeight) * oversample - textPaint.descent();

								canvas.drawText(txt, x, y, contourPaint);
								canvas.drawText(txt, x, y, textPaint);
							}
							charIndex++;
						}

						lineCounter++;
						offset += codepointCharCount;
					}
					SystemTextLine line = new SystemTextLine(bitmap);
					line.setVisible(getVisible());
					lineImage.add(line);
				} else {
					lineImage.add(new SystemTextLine());
				}
				startLine = nextLine;
			}
			
/*			Log.d("SystemText", Utils.format(Locale.ROOT,
					"%3.1f x %3.1f (max: %3.1f, lines: %d) -> %s", width,
					height, maxWidth / scale.x, lineImage.size(), text));
*/
		}
	}

	@Override
	protected void updateMatrix() {
		// "origin" field is ignored
		Matrix.setIdentity(matrix);
		Matrix.translate(matrix, x, y);
		Matrix.scale(matrix, scale.x, scale.y);
		Matrix.rotate(matrix, angle);
	}

	private void updateParent() {
		Group parent = getParent();
		for (SystemTextLine img : lineImage) {
			if (img.getParent() != parent) {
				if (img.getParent() != null) {
					img.getParent().remove(img);
				}

				if (parent != null) {
					parent.add(img);
				}
			}
		}
	}

	@Override
	public void setParent(@NonNull Group parent) {
		super.setParent(parent);

		updateParent();
	}

	@Override
	public boolean setVisible(boolean visible) {
		if (lineImage != null) {
			for (SystemTextLine img : lineImage) {
				img.setVisible(visible);
			}
		}
		return super.setVisible(visible);
	}

	@Override
	public void draw() {
		measure();
		if (lineImage != null) {
			int line = 0;

			updateParent();

			for (SystemTextLine img : lineImage) {

				img.ra = ra;
				img.ga = ga;
				img.ba = ba;
				img.rm = rm;
				img.gm = gm;
				img.bm = bm;
				img.am = am;
				img.aa = aa;

				img.setPos(x, y + (line * fontHeight) * scale.y);
				img.setScale(scale.x / oversample, scale.x / oversample);

				line++;
			}
		}
	}

	private float symbolWidth(String symbol) {
		return textPaint.measureText(symbol) / oversample;
	}

	public void measure() {
		if (Math.abs(scale.x) < 0.001) {
			return;
		}

		if (dirty) {
			dirty = false;
			if (text == null) {
				text = "";
			}

			fontHeight = (textPaint.descent() - textPaint.ascent())
					/ oversample;
			createText();
		}
	}

	public String text() {
		return text;
	}

	public void text(String str) {
		dirty = true;
		text = str;
		measure();
	}

	@Override
	public float baseLine() {
		return height * scale.y;
	}

	static void invalidate() {
		for (SystemText txt : texts) {
			txt.dirty = true;
			txt.destroyLines();
		}
	}
}
