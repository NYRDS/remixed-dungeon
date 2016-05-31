package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.items.accessories.Accessory;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.ui.ItemSlot;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.Utils;

public class WndHatInfo extends Window {

	private static final int WIDTH = 120;
	private static final int MARGIN = 2;
	private static final float GAP	= 2;

	private static final int WIDTH_P = 120;
	private static final int WIDTH_L = 160;

	public WndHatInfo( final Accessory accessory ) {

		int yPos = 0;

		Text tfTitle = PixelScene.createMultiline("Hat", 9);
		tfTitle.hardlight(TITLE_COLOR);
		tfTitle.x = tfTitle.y = MARGIN;
		tfTitle.maxWidth(WIDTH - MARGIN * 2);
		tfTitle.measure();

		yPos += tfTitle.height() + MARGIN;
		add(tfTitle);

		resize( WIDTH,  yPos);



	}
}