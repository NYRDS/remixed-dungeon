

package com.watabou.gltextures;

import com.nyrds.platform.compatibility.RectF;
import com.nyrds.platform.gfx.BitmapData;
import com.nyrds.platform.gl.Texture;

import org.jetbrains.annotations.NotNull;

public class SmartTexture extends Texture {

	public int width;
	public int height;

	public Atlas atlas;

	public SmartTexture() {
		super();
	}

	public SmartTexture(@NotNull BitmapData bitmap ) {
		this( bitmap, NEAREST, CLAMP );
	}

	public SmartTexture(@NotNull BitmapData bitmap, int filtering, int wrapping ) {
		super();

		bitmap( bitmap );
		filter( filtering, filtering );
		wrap( wrapping, wrapping );
	}

	@Override
	public void bitmap( BitmapData bitmap ) {
		width = bitmap.getWidth();
		height = bitmap.getHeight();
		super.bitmap( bitmap );
	}

	/**
	 * Set whether bitmap data should be automatically disposed after being uploaded as a texture
	 * @param autoDispose True to dispose after upload (default), false to preserve bitmap data
	 */
	public static void setAutoDisposeBitmapData(boolean autoDispose) {
		Texture.setAutoDisposeBitmapData(autoDispose);
	}

	/**
	 * Get whether bitmap data is automatically disposed after being uploaded as a texture
	 * @return True if bitmap data is disposed after upload, false otherwise
	 */
	public static boolean getAutoDisposeBitmapData() {
		return Texture.getAutoDisposeBitmapData();
	}

	public RectF uvRect( int left, int top, int right, int bottom ) {
		return new RectF(
				(float)left / width,
				(float)top	/ height,
				(float)right / width,
				(float)bottom / height );
	}

	/**
	 * Get the bitmap data associated with this texture
	 * @return The bitmap data, or null if it's not available
	 */
	public BitmapData getBitmapData() {
		return this.bitmapData;
	}
}
