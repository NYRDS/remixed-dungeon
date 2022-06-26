package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.items.accessories.Accessory;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.windows.WndHelper;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.game.RemixedDungeon;
import com.nyrds.platform.gfx.SystemText;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Image;
import com.watabou.noosa.Text;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.ScrollPane;
import com.watabou.pixeldungeon.ui.TextButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.Utils;

import java.util.Collections;
import java.util.List;

public class WndHats extends Window {

	private static final int WIDTH            = 120;
	private static final int HEIGHT_PORTRAIT  = 180;
	private static final int HEIGHT_LANDSCAPE = (int) PixelScene.MIN_HEIGHT_L;

	public Image slot;

	public WndHats() {

		EventCollector.logScene(getClass().getCanonicalName());

		int yPos = 0;

		String equippedName = Utils.EMPTY_STRING;

		if (updateSlotImage()) {
			equippedName = ": " + Accessory.equipped().name();
		}

		//"Equipped Accessory" slot
		//Title
        Text slotTitle = PixelScene.createMultiline(StringsManager.getVar(R.string.WndHats_SlotTitle) + equippedName, GuiProperties.titleFontSize());
		slotTitle.hardlight(0xFFFFFF);
		slotTitle.maxWidth(WIDTH - GAP * 2);
		slotTitle.setX((WIDTH - slotTitle.width()) / 2);
		slotTitle.setY(GAP);
		add(slotTitle);

		//Image
		slot.setPos(GAP, slotTitle.height() + GAP * 2);
		add(slot);

		//Unequip Button
        TextButton sb = new RedButton(R.string.WndHats_UnequipButton) {
			@Override
			protected void onClick() {
				super.onClick();
				Accessory.unequip();
				onBackPressed();
				GameScene.show(new WndHats());
			}
		};

		sb.setRect(slot.getX() + slot.width() * 2 + GAP, slot.getY(), slot.width() * 2, slot.height() / 2);

		add(sb);

		//List of Accessories
		//Title
        Text listTitle = PixelScene.createMultiline(R.string.WndHats_ListTitle, GuiProperties.titleFontSize());
		listTitle.hardlight(TITLE_COLOR);
		listTitle.maxWidth(WIDTH - GAP * 2);
		listTitle.setX((WIDTH - listTitle.width()) / 2);
		listTitle.setY(slot.getY() + slot.height() + GAP * 2);

		add(listTitle);

		List<String> hats = Accessory.getAccessoriesList();
		Collections.shuffle(hats);

		Component content = new Component();

		//List
		for (final String item : hats) {
			Accessory accessory = Accessory.getByName(item);

			String price = Utils.EMPTY_STRING;

			if (accessory.haveIt()) {
                price = StringsManager.getVar(R.string.WndHats_Purchased);
			} else if(!accessory.nonIap()) {
				price = RemixedDungeon.instance().iap.getSkuPrice(item);
			}

			//Image
			Image hat = accessory.getImage();
			hat.setPos(GAP, yPos);
			content.add(hat);

			//Text
			String hatName = Accessory.getByName(item).name();

			Text name = PixelScene.createMultiline(hatName, GuiProperties.regularFontSize());

			name.hardlight(0xFFFFFF);

			name.setY(hat.getY() + GAP);
			name.maxWidth(WIDTH - GAP);
			name.setX(hat.getX() + hat.width() + GAP);

			content.add(name);
			float rbY = name.bottom() + GAP * 2;

			//Pricetag
			SystemText priceTag = new SystemText(price, GuiProperties.regularFontSize(), false);

			priceTag.hardlight(0xFFFF00);
			priceTag.setY(name.bottom() + GAP);
			priceTag.maxWidth((int) hat.width());
			priceTag.setX(name.getX());

			content.add(priceTag);

            String buttonText = StringsManager.getVar(R.string.WndHats_InfoButton);
			final Accessory finalAccessory = accessory;

			if (accessory.haveIt() && accessory.usableBy(Dungeon.hero)) {
                buttonText = StringsManager.getVar(R.string.WndHats_EquipButton);
			}

			final Window currentWindow = this;

			//Button
			final String finalPrice = price;
			TextButton rb = new RedButton(buttonText) {
				@Override
				protected void onClick() {
					super.onClick();

					if (finalAccessory.haveIt()) {
						finalAccessory.equip();
						onBackPressed();
						return;
					}
					GameScene.show(new WndHatInfo(item, finalPrice, currentWindow));
				}
			};

			rb.setRect(slot.getX() + slot.width() * 2 + GAP, rbY, slot.width() * 2, slot.height() / 2);

			content.add(rb);
			yPos = (int) (rb.bottom() + GAP * 2);
		}

		int HEIGHT = WndHelper.getAlmostFullscreenHeight();
		int h = Math.min(HEIGHT - GAP, yPos);

		float topGap = listTitle.getY() + listTitle.height() + GAP;
		float BottomGap = slotTitle.height() + slot.height() + listTitle.height() + GAP * 5;

		resize(WIDTH, h);

		content.setSize(WIDTH, yPos);
		ScrollPane list = new ScrollPane(content);
		list.dontCatchTouch();

		add(list);

		list.setRect(0, topGap, WIDTH, HEIGHT - BottomGap);
	}

	public boolean updateSlotImage() {
		if (Accessory.equipped() != null) {
			slot = Accessory.equipped().getImage();
			return true;
		} else {
			slot = Accessory.getSlotImage();
			return false;
		}
	}
}