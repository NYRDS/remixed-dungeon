

package com.watabou.noosa.tweeners;

import com.watabou.noosa.Visual;

public class AlphaTweener extends Tweener {

	public final Visual image;

	private final float start;
	private final float delta;
	
	public AlphaTweener( Visual image, float alpha, float time ) {
		super( image, time );
		
		this.image = image;
		start = image.alpha();
		delta = alpha - start;
	}

	@Override
	protected void updateValues( float progress ) {
		image.alpha( start + delta * progress );
	}
}
