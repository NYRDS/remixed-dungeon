package com.nyrds.platform.gl;

import com.nyrds.platform.gfx.BitmapData;

// headless: textures track their metadata, pixel data is dropped
public class Texture {

	public static final int NEAREST = 1;
	public static final int LINEAR = 2;
	public static final int REPEAT = 3;
	public static final int MIRROR = 4;
	public static final int CLAMP = 5;

	protected int id = -1;

	private static final int[] bound = new int[32];
	private static int active = 0;

	protected BitmapData bitmapData;
	private boolean recode;
	private int minFilter = LINEAR;
	private int maxFilter = LINEAR;
	private int wrapS = CLAMP;
	private int wrapT = CLAMP;
	private int[] pixels;
	protected int width;
	protected int height;
	private byte[] bytePixels;
	private boolean dataDirty = false;

	private static boolean autoDisposeBitmapData = true;

	public Texture() {
	}

	public static void activate(int index) {
		active = index;
	}

	public void bind() {
		if (id == -1) {
			id = 1;
		}

		if (bound[active] != id) {
			bound[active] = id;
			uploadPixelData();
		}
	}

	private void uploadPixelData() {
		if (dataDirty) {
			if (bitmapData != null) {
				width = bitmapData.getWidth();
				height = bitmapData.getHeight();
				if (autoDisposeBitmapData) {
					bitmapData.dispose();
				}
				bitmapData = null;
			}
			pixels = null;
			bytePixels = null;
			dataDirty = false;
		}
	}

	static public void unbind() {
		bound[active] = -1;
	}

	public void filter(int minMode, int maxMode) {
		this.minFilter = minMode;
		this.maxFilter = maxMode;
	}

	public void wrap(int s, int t) {
		this.wrapS = s;
		this.wrapT = t;
	}

	public void delete() {
		if (id != -1) {
			id = -1;
			dataDirty = true;
		}
	}

	public void bitmap(BitmapData bitmap) {
		this.bitmapData = bitmap;
		this.recode = true;
		this.dataDirty = true;
	}

	public static void setAutoDisposeBitmapData(boolean autoDispose) {
		autoDisposeBitmapData = autoDispose;
	}

	public static boolean getAutoDisposeBitmapData() {
		return autoDisposeBitmapData;
	}

	public void pixels(int w, int h, int[] pixels) {
		this.width = w;
		this.height = h;
		this.pixels = pixels;
		this.bytePixels = null;
		this.bitmapData = null;
		this.dataDirty = true;
	}

	public void pixels(int w, int h, byte[] pixels) {
		this.width = w;
		this.height = h;
		this.bytePixels = pixels;
		this.pixels = null;
		this.bitmapData = null;
		this.dataDirty = true;
	}

	public BitmapData getBitmapData() {
		return this.bitmapData;
	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}
}
