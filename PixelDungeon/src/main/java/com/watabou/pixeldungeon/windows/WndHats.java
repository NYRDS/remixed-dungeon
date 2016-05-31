package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.items.accessories.Accessory;
import com.nyrds.pixeldungeon.support.Iap;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;

import java.util.List;

public class WndHats extends Window {

	private static final int WIDTH = 120;
	private static final int MARGIN = 2;
	private static final int BUTTON_HEIGHT = 20;

	public WndHats() {

		int yPos = 0;

		Text tfTitle = PixelScene.createMultiline("Accessories", 9);
		tfTitle.hardlight(TITLE_COLOR);
		tfTitle.x = tfTitle.y = MARGIN;
		tfTitle.maxWidth(WIDTH - MARGIN * 2);
		tfTitle.measure();

		yPos += tfTitle.height() + MARGIN;
		add(tfTitle);

		List<String> hats = Accessory.getAccessoriesList();

		for (final String item: hats) {
			String price = Iap.getSkuPrice(item);
			if(price!=null) {
				RedButton rb = new RedButton(item + " " + price) {
					@Override
					protected void onClick() {
						super.onClick();
						Iap.doPurchase(item);
					}
				};

				rb.setRect(0,yPos,WIDTH - MARGIN * 2, BUTTON_HEIGHT );

				add(rb);
				yPos += rb.height() + MARGIN;
			}
		}

		resize( WIDTH,  yPos);
	}
}