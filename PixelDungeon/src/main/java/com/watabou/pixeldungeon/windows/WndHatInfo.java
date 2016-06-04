package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.items.accessories.Accessory;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.support.Iap;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.SystemText;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.sprites.HeroSpriteDef;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.TextButton;
import com.watabou.pixeldungeon.ui.Window;

public class WndHatInfo extends Window {

	private static final int WIDTH = 100;
	private static final int HEIGHT = 160;
	private static final int MARGIN = 2;
	private static final int BUTTON_HEIGHT = 16;

	public WndHatInfo(final String accessory, String price, final Window parent ) {
		int yPos = 0;

		final Accessory item = Accessory.getByName(accessory);

		// Dummy Hero
		Hero hero = new Hero();
		hero.heroClass = Dungeon.hero.heroClass;
		hero.subClass = Dungeon.hero.subClass;
		hero.belongings = Dungeon.hero.belongings;
		hero.setPos(Dungeon.hero.getPos());
		hero.setSprite(new HeroSpriteDef(hero, item));

		// Title
		Text tfTitle = PixelScene.createMultiline(item.name(), 11);
		tfTitle.hardlight(TITLE_COLOR);
		tfTitle.maxWidth(WIDTH - MARGIN * 2);
		tfTitle.measure();
		tfTitle.x = (WIDTH - tfTitle.width())/2;
		tfTitle.y = MARGIN;
		add(tfTitle);

		yPos += tfTitle.height() + MARGIN;

		if(price!=null) {
			//Pricetag
			SystemText priceTag = new SystemText(12);
			priceTag.text(price);

			priceTag.hardlight(0xFFFF00);
			priceTag.maxWidth(WIDTH - MARGIN * 2);
			priceTag.measure();
			priceTag.x = (WIDTH - priceTag.width()) / 2;
			priceTag.y = yPos;
			add(priceTag);

			yPos += priceTag.height() + MARGIN * 2;
		}

		//Preview Image
		Image preview = hero.getHeroSprite().avatar();
		preview.setPos(WIDTH / 4 - preview.width() / 2,yPos);
		preview.setScale(4, 4);
		add(preview);
		yPos += preview.height() + MARGIN * 4;

		//Button
		String buttonText = Game.getVar(R.string.WndHats_BuyButton);
		if(item.haveIt()) {
			buttonText = Game.getVar(R.string.WndHats_BackButton);
		}

		TextButton rb = new RedButton(buttonText) {
			@Override
			protected void onClick() {
				super.onClick();

				if(item.haveIt()) {
					onBackPressed();
					return;
				}

				Iap.doPurchase(accessory, new Iap.IapCallback() {
					@Override
					public void onPurchaseOk() {
						item.ownIt(true);
						item.equip();
						Dungeon.hero.updateLook();
						onBackPressed();
						parent.hide();
					}
				});
			}
		};

		if(!item.haveIt() && price == null) {
			rb.enable(false);
		}

		rb.setRect(WIDTH / 4, preview.y + preview.height() + MARGIN * 2, WIDTH / 2, BUTTON_HEIGHT );

		yPos += BUTTON_HEIGHT + MARGIN;
		add(rb);

		//Resizing window
		int h = Math.min(HEIGHT - MARGIN, yPos);
		resize( WIDTH,  h);
	}
}