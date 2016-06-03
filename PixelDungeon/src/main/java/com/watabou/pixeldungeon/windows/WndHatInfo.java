package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.items.accessories.Accessory;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.support.Iap;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.SystemRedButton;
import com.watabou.pixeldungeon.ui.TextButton;
import com.watabou.pixeldungeon.ui.Window;

public class WndHatInfo extends Window {

	private static final int WIDTH = 100;
	private static final int HEIGHT = 160;
	private static final int MARGIN = 2;
	private static final int BUTTON_HEIGHT = 16;

	public WndHatInfo(final String accessory, String text ) {
		int yPos = 0;

		final Accessory item = Accessory.getByName(accessory);

		Text tfTitle = PixelScene.createMultiline(item.name(), 11);
		tfTitle.hardlight(TITLE_COLOR);
		tfTitle.maxWidth(WIDTH - MARGIN * 2);
		tfTitle.measure();
		tfTitle.x = (WIDTH - tfTitle.width())/2;
		tfTitle.y = MARGIN;

		yPos += tfTitle.height() + MARGIN;
		add(tfTitle);

		Image hat = item.getImage();
		hat.setPos(0,yPos);
		add(hat);

		Text info = PixelScene.createMultiline(item.desc(), 9 );

		info.hardlight(0xFFFFFF);
		info.x = hat.x + hat.width();
		info.y = hat.y;
		info.maxWidth(WIDTH - (int)hat.width());
		info.measure();

		yPos += info.height() + MARGIN;
		add(info);

		String buttonText = text;

		if(item.haveIt()) {
			buttonText = Game.getVar(R.string.WndHats_EquipButton);
		}

		TextButton rb = new SystemRedButton(buttonText) {
			@Override
			protected void onClick() {
				super.onClick();

				if(item.haveIt()) {
					item.equip();
					Dungeon.hero.updateLook();
					return;
				}

				Iap.doPurchase(accessory, new Iap.IapCallback() {
					@Override
					public void onPurchaseOk() {
						item.ownIt(true);
						text(Game.getVar(R.string.WndHats_EquipButton));
					}
				});
			}
		};

		rb.setRect(hat.width(),info.y + info.height() + MARGIN, WIDTH / 2, BUTTON_HEIGHT );

		yPos += BUTTON_HEIGHT + MARGIN;
		add(rb);

		int h = Math.min(HEIGHT - MARGIN, yPos);

		resize( WIDTH,  h);
	}
}