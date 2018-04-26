package com.watabou.pixeldungeon.ui;

import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Button;

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

}
