

package com.watabou.gltextures;

import com.nyrds.platform.EventCollector;
import com.nyrds.platform.compatibility.RectF;
import com.nyrds.platform.gfx.BitmapData;
import com.nyrds.platform.gl.Texture;
import org.jetbrains.annotations.NotNull;

public class SmartTexture extends Texture {

	public Atlas atlas;

	private HealSource healSource;

	/**
	 * Optional self-heal source (bd 3tf option D): recreates the texture's pixels
	 * on demand when the upload data has been consumed or released (destructive
	 * clear + stale-holder rebind) or the GL name died (context loss) - instead of
	 * rendering an incomplete (black) texture. Suppliers decode transiently: no
	 * bitmaps are retained between binds, so the leak-round memory wins hold.
	 */
	public interface HealSource {
		BitmapData recreate();
	}

	public void setHealSource(HealSource src) {
		healSource = src;
	}

	@Override
	protected boolean canHeal() {
		return healSource != null;
	}

	@Override
	protected BitmapData healPixels() {
		if (healSource == null) {
			return null;
		}
		try {
			BitmapData healed = healSource.recreate();
			if (healed == null) {
				throw new Exception("heal source returned nothing");
			}
			return healed;
		} catch (Throwable t) {
			EventCollector.logException(t, "texture self-heal decode failed");
			healSource = null; // decode failures are deterministic - never retry per frame
			return null;
		}
	}

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
				(float)left / getWidth(),
				(float)top	/ getHeight(),
				(float)right / getWidth(),
				(float)bottom / getHeight() );
	}
}
