package com.watabou.pixeldungeon.ui;

import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
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


	@Override
	protected void layout() {
		super.layout();

		image.x = x;
		image.y = y;
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

}
