package com.watabou.pixeldungeon.windows;

import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.Window;

public class WndHats extends Window {

	private static final int WIDTH = 120;
	private static final int MARGIN = 2;

	public WndHats() {

		int yPos = 0;

		Text tfTitle = PixelScene.createMultiline("Accessory inventory", 9);
		tfTitle.hardlight(TITLE_COLOR);
		tfTitle.x = tfTitle.y = MARGIN;
		tfTitle.maxWidth(WIDTH - MARGIN * 2);
		tfTitle.measure();

		yPos += tfTitle.height() + MARGIN;
		add(tfTitle);

		resize( WIDTH,  yPos);
	}
}