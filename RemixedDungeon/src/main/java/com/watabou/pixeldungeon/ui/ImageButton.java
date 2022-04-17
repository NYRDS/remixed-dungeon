package com.watabou.pixeldungeon.ui;

import com.nyrds.platform.audio.Sample;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Button;
import com.watabou.pixeldungeon.Assets;

public class ImageButton extends Button {

	protected Image image;

	public ImageButton(Image _image) {
		super();

		image=_image;

        add( image );

        width = image.width;
		height = image.height;
	}

	public void brightness( float val ) {
		image.brightness(val);
	}

	@Override
	protected void layout() {
		super.layout();

		image.setX(x);
		image.setY(y);
	}

	@Override
	protected void onTouchDown() {
		image.brightness( 1.5f );
		Sample.INSTANCE.play( Assets.SND_CLICK, 0.7f, 0.7f, 1.2f );
	}

	@Override
	protected void onTouchUp() {
		image.resetColor();
	}

	public void enable( boolean value ) {
		setActive(value);
		image.alpha( value ? 1.0f : 0.3f );
	}
}
