package com.watabou.noosa;

import java.util.ArrayList;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.Log;

import com.watabou.glwrap.Matrix;

public class SystemText extends Text {

	protected String text;

	protected TextPaint textPaint = new TextPaint();

	private ArrayList<Image> lineImage = new ArrayList<Image>();
	private ArrayList<Float> yPos = new ArrayList<Float>();

	private int size = 8;
	private final int oversample = 4;

	public SystemText() {
		this("", null);
	}

	public SystemText(Font font) {
		this("", font);
	}

	public SystemText(String text, Font font) {
		super(0, 0, 0, 0);
		size = (int) font.baseLine;

		if (size == 0) {
			try {
				throw new Exception("zero sized font!!!");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		Typeface tf = Typeface.create((String) null, Typeface.BOLD);

		textPaint.setTextSize(size * oversample);
		textPaint.setAntiAlias(true);
		textPaint.setColor(0xffffffff);
		textPaint.setTypeface(tf);

		this.text(text);
	}

	@Override
	public void destroy() {
		text = null;
		super.destroy();
	}

	@SuppressLint("NewApi")
	private void createText() {
		measure();
		if (width > 0 && height > 0) {

			if (parent != null) {
				for (Image img : lineImage) {
					parent.remove(img);
				}
				lineImage.clear();
			}

			Log.d("SystemText", String.format(Locale.ROOT,
					"%3.1f x %3.1f -> %s", width, height, text));

			Bitmap bitmap = Bitmap.createBitmap((int) (width * oversample),
					(int) (height * oversample), Bitmap.Config.ARGB_4444);
			Canvas canvas = new Canvas(bitmap);

			float x = 0;
			for (int i = 0; i < text.length(); i++) {
				if (mask == null || mask[i]) {
					canvas.drawText(text.substring(i, i + 1), x,
							textPaint.descent() * oversample, textPaint);
				}
				x += symbolWidth(text.substring(i, i + 1)) * oversample;
			}
			// canvas.drawText(text, 0, textPaint.descent()*oversample,
			// textPaint);

			lineImage.add(new Image(bitmap, true));
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

	@SuppressLint("NewApi")
	@Override
	public void draw() {
		super.draw();

		measure();
		if (lineImage != null) {
			for (Image img : lineImage) {

				//Log.d("SystemText", String.format(Locale.ROOT,
				//		"%3.1f x %3.1f -> %s", x, y, text));

				if (parent!=null && img.parent != parent) {
					if (img.parent != null) {
						img.parent.remove(img);
					}
					parent.add(img);
				}

				img.x = x;
				img.y = y;

				img.scale.x = scale.x / oversample;
				img.scale.y = scale.y / oversample;

			}
		}
	}

	protected float symbolWidth(String symbol) {
		return textPaint.measureText(symbol) / oversample;
	}

	public void measure() {
		if (dirty) {
			dirty = false;
			if (text.equals("")) {
				return;
			}

			width = 0;
			for (int i = 0; i < text.length(); i++) {
				width += symbolWidth(text.substring(i, i + 1));
			}

			// width = textPaint.measureText(text)/oversample;
			height = (textPaint.descent() - textPaint.ascent()) / oversample;
		}
	}

	public String text() {
		return text;
	}

	public void text(String str) {
		dirty = true;
		if (str == null) {
			text = "";
		} else {
			text = str;
		}
		createText();
	}

	@Override
	public float baseLine() {
		return height * scale.y;
	}
}
