package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.items.accessories.Accessory;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.support.Iap;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.Text;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.ui.ItemSlot;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.ScrollPane;
import com.watabou.pixeldungeon.ui.SystemRedButton;
import com.watabou.pixeldungeon.ui.TextButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.Utils;

import java.util.List;

public class WndHatInfo extends Window {

	private static final int WIDTH = 100;
	private static final int HEIGHT = 160;
	private static final int MARGIN = 2;
	private static final int BUTTON_HEIGHT = 16;

	public WndHatInfo(final String accessory, String price ) {
		int yPos = 0;

		Text tfTitle = PixelScene.createMultiline(Accessory.getByName(accessory).name(), 11);
		tfTitle.hardlight(TITLE_COLOR);
		tfTitle.maxWidth(WIDTH - MARGIN * 2);
		tfTitle.measure();
		tfTitle.x = (WIDTH - tfTitle.width())/2;
		tfTitle.y = MARGIN;

		yPos += tfTitle.height() + MARGIN;
		add(tfTitle);

		Image hat = Accessory.getByName(accessory).getImage();
		hat.setPos(0,yPos);
		add(hat);

		Text info = PixelScene.createMultiline(Accessory.getByName(accessory).desc(), 9 );

		info.hardlight(0xFFFFFF);
		info.x = hat.x + hat.width();
		info.y = hat.y;
		info.maxWidth(WIDTH - (int)hat.width());
		info.measure();

		yPos += info.height() + MARGIN;
		add(info);

		TextButton rb = new SystemRedButton(price) {
			@Override
			protected void onClick() {
				super.onClick();
				Iap.doPurchase(accessory);
			}
		};

		rb.setRect(hat.width(),info.y + info.height() + MARGIN, WIDTH / 2, BUTTON_HEIGHT );

		yPos += BUTTON_HEIGHT + MARGIN;
		add(rb);

		int h = Math.min(HEIGHT - MARGIN, yPos);

		resize( WIDTH,  h);


/*
		int yPos = 0;

		Text tfTitle = PixelScene.createMultiline("Hat", 9);
		tfTitle.hardlight(TITLE_COLOR);
		tfTitle.x = tfTitle.y = MARGIN;
		tfTitle.maxWidth(WIDTH - MARGIN * 2);
		tfTitle.measure();

		yPos += tfTitle.height() + MARGIN;
		add(tfTitle);

		resize( WIDTH,  yPos);
*/
	}
}