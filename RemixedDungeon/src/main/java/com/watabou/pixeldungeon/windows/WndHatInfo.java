package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.items.accessories.Accessory;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.game.RemixedDungeon;
import com.nyrds.platform.gfx.SystemText;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.nyrds.util.ModdingMode;
import com.watabou.noosa.Image;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.sprites.HeroSpriteDef;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.TextButton;
import com.watabou.pixeldungeon.ui.Window;

public class WndHatInfo extends Window {

	private static final int WIDTH         = 110;
	private static final int HEIGHT        = 160;
	private static final int BUTTON_HEIGHT = 16;

	public WndHatInfo(final String accessory, String price, final Window parent ) {
		final Accessory item = Accessory.getByName(accessory);

		EventCollector.logScene(getClass().getCanonicalName()+":"+item.getClass().getSimpleName());

		// Title
		Text tfTitle = PixelScene.createMultiline(item.name(), GuiProperties.mediumTitleFontSize());
		tfTitle.hardlight(TITLE_COLOR);
		tfTitle.maxWidth(WIDTH - GAP);
		tfTitle.setX((WIDTH - tfTitle.width())/2);
		tfTitle.setY(GAP);
		add(tfTitle);

		//Pricetag
		SystemText priceTag = new SystemText(price,GuiProperties.mediumTitleFontSize(),false);

		priceTag.hardlight(0xFFFF00);
		priceTag.maxWidth(WIDTH - GAP);
		priceTag.setX((WIDTH - priceTag.width()) / 2);
		priceTag.setY(tfTitle.bottom() + GAP);
		add(priceTag);

		//Preview Image
		Image preview = (HeroSpriteDef.createHeroSpriteDef(Dungeon.hero, item)).avatar();
		preview.setPos(WIDTH / 2 - preview.width(), priceTag.bottom() + GAP);
		preview.setScaleXY(2, 2);
		add(preview);

		//Text
		String hatText = Accessory.getByName(accessory).desc();

		Text info = PixelScene.createMultiline(hatText, GuiProperties.regularFontSize());

		info.hardlight(0xFFFFFF);

		info.setY(preview.bottom() + GAP);
		info.maxWidth(WIDTH - GAP);
		info.setX((WIDTH - info.width()) / 2);

		add(info);

		//Button
        String buttonText = StringsManager.getVar(R.string.WndHats_BuyButton);
		if(item.haveIt() || price.isEmpty()) {
            buttonText = StringsManager.getVar(R.string.WndHats_BackButton);
		}

		TextButton rb = new RedButton(buttonText) {
			@Override
			protected void onClick() {
				super.onClick();

				if(item.haveIt()||price.isEmpty()) {
					onBackPressed();
					return;
				}

				GameLoop.runOnMainThread(
						() -> {
							EventCollector.logEvent("PurchaseClick",item.name());
							RemixedDungeon.instance().iap.doPurchase(accessory, () -> {
								item.ownIt(true);
								item.equip();
								onBackPressed();
								if(parent!=null) {
									parent.hide();
								}
								if(!Game.isPaused()) {
									GameScene.show(new WndHats());
								}
							});
						}
				);
			}
		};

		rb.enable(!ModdingMode.useRetroHeroSprites);

		if(!item.haveIt() && price == null) {
			rb.enable(false);
		}

		rb.setRect(WIDTH / 4, info.bottom() + GAP, WIDTH / 2, BUTTON_HEIGHT);
		add(rb);

		//Resizing window
		int h = Math.min(HEIGHT - GAP, (int) rb.bottom());
		resize( WIDTH,  h);
	}
}