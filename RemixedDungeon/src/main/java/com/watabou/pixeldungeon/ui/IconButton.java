package com.watabou.pixeldungeon.ui;

import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.scenes.PixelScene;

public class IconButton extends RedButton {

	public IconButton(int label, Image icon) {
		super(label);
		icon( icon );
	}

	@Override
	protected void layout() {
		super.layout();
		
		float margin = (height - text.baseLine()) / 2;
		
		text.x = PixelScene.align( PixelScene.uiCamera, x + margin );
		text.y = PixelScene.align( PixelScene.uiCamera, y + margin );
		
		icon.x = PixelScene.align( PixelScene.uiCamera, x + width - margin - icon.width );
		icon.y = PixelScene.align( PixelScene.uiCamera, y + (height - icon.height()) / 2 );
	}
}
