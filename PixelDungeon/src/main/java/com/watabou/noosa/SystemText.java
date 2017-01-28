package com.watabou.noosa;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextPaint;

import com.nyrds.android.util.TrackedRuntimeException;
import com.watabou.glwrap.Matrix;
import com.watabou.pixeldungeon.PixelDungeon;

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

	private static float fontScale = Float.NaN;

	public SystemText(float baseLine) {
		this("", baseLine, false);
	}

	public SystemText(String text, float size, boolean multiline) {
		super(0, 0, 0, 0);

		if (fontScale != fontScale) {
			updateFontScale();
		}

		if (tf == null) {
			if (Game.smallResScreen()) {
				tf = Typeface.create((String) null, Typeface.BOLD);
				oversample = 1;
			} else {
				tf = Typeface.create((String) null, Typeface.NORMAL);
				oversample = 4;
			}
		}

		size *= fontScale;

		needWidth = multiline;

		if (size == 0) {
			throw new TrackedRuntimeException("zero sized font!!!");
		}

		float textSize = size * oversample;
		if (!textPaints.containsKey(textSize)) {
			TextPaint tx = new TextPaint();

			tx.setTextSize(textSize);
			tx.setStyle(Paint.Style.FILL);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				//tx.setHinting(Paint.HINTING_ON);
			}
			tx.setAntiAlias(true);

			tx.setColor(Color.WHITE);

			tx.setTypeface(tf);

			TextPaint cp = new TextPaint();
			cp.set(tx);
			cp.setStyle(Paint.Style.STROKE);
			cp.setStrokeWidth(textSize * 0.2f);
			cp.setColor(Color.BLACK);
			cp.setAntiAlias(true);

			textPaints.put(textSize, tx);
			contourPaints.put(textSize, cp);
		}

		textPaint = textPaints.get(textSize);
		contourPaint = contourPaints.get(textSize);

		this.text(text);
		texts.add(this);
	}

	public static void updateFontScale() {
		float scale = 0.5f + 0.01f * PixelDungeon.fontScale();

		scale *= 1.2f;

		if (scale < 0.1f) {
			fontScale = 0.1f;
			return;
		}
		if (scale > 4) {
			fontScale = 4f;
			return;
		}

		if (Game.smallResScreen()) {
			scale *= 1.5;
		}

		fontScale = scale;
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

	private ArrayList<Float>   xCharPos   = new ArrayList<>();
	private ArrayList<Integer> codePoints = new ArrayList<>();
	private String currentLine;

	private float fontHeight;

	private float lineWidth;

	private int fillLine(int startFrom) {
		int offset = startFrom;

		float xPos = 0;
		lineWidth = 0;
		xCharPos.clear();
		codePoints.clear();

		final int length = text.length();
		int lastWordOffset = offset;

		int codepoint = 0;
		int lastWordStart = 0;

		for (; offset < length; ) {

			codepoint = text.codePointAt(offset);
			int codepointCharCount = Character.charCount(codepoint);
			offset += codepointCharCount;

			if (Character.isWhitespace(codepoint)) {
				lastWordOffset = offset;
				lastWordStart = xCharPos.size();
			} else {
				xCharPos.add(xPos);
				codePoints.add(codepoint);
			}

			if (codepoint == 0x000A) {
				return offset;
			}

			xPos += symbolWidth(Character.toString((char) (codepoint)));
			lineWidth = xPos;

			if (maxWidth != Integer.MAX_VALUE
					&& xPos > maxWidth / scale.x) {
				if (lastWordOffset != startFrom) {
					xCharPos.subList(lastWordStart, xCharPos.size()).clear();
					codePoints.subList(lastWordStart, codePoints.size()).clear();
					return lastWordOffset;
				} else {
					xCharPos.remove(xCharPos.size() - 1);
					codePoints.remove(codePoints.size() - 1);
					return offset - 1;
				}
			}
		}

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
			width = 0;

			height = fontHeight / 4;

			int charIndex = 0;
			int startLine = 0;

			while (startLine < text.length()) {

				int nextLine = fillLine(startLine);

				height += fontHeight;

				if (lineWidth > 0) {

					lineWidth += 1;
					width = Math.max(lineWidth, width);

					Bitmap bitmap = Bitmap.createBitmap(
							(int) (lineWidth * oversample),
							(int) (fontHeight * oversample),
							Bitmap.Config.ARGB_4444);

					Canvas canvas = new Canvas(bitmap);

					currentLine = text.substring(startLine,nextLine);

					drawTextLine(charIndex, canvas, contourPaint);
					charIndex = drawTextLine(charIndex, canvas, textPaint);

					SystemTextLine line = new SystemTextLine(bitmap);
					line.setVisible(getVisible());
					lineImage.add(line);
				} else {
					lineImage.add(new SystemTextLine());
				}
				startLine = nextLine;
			}
		}
	}

	private int drawTextLine(int charIndex, Canvas canvas, TextPaint paint) {

		float y = (fontHeight) * oversample - textPaint.descent();

		final int charsToDraw = codePoints.size();

		if (mask == null) {
			float x = (xCharPos.get(0) + 0.5f) * oversample;
			canvas.drawText(currentLine, x, y, paint);
			return charIndex + codePoints.size();
		}

		for (int i = 0; i < charsToDraw; ++i) {
			int codepoint = codePoints.get(i);

			if (charIndex < mask.length && mask[charIndex]) {

				float x = (xCharPos.get(i) + 0.5f) * oversample;

				canvas.drawText(Character.toString((char) codepoint), x, y, paint);
			}
			charIndex++;
		}

		return charIndex;
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
