package com.watabou.pixeldungeon.windows;

import com.nyrds.android.util.GuiProperties;
import com.nyrds.pixeldungeon.items.accessories.Accessory;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.support.Iap;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.SystemText;
import com.watabou.noosa.Text;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.ScrollPane;
import com.watabou.pixeldungeon.ui.TextButton;
import com.watabou.pixeldungeon.ui.Window;

import java.util.List;

public class WndHats extends Window {

	private static final int WIDTH            = 120;
	private static final int HEIGHT_PORTRAIT  = 180;
	private static final int HEIGHT_LANDSCAPE = (int) PixelScene.MIN_HEIGHT_L;
	private static final int MARGIN           = 2;

	public Image slot;

	public WndHats() {

		EventCollector.logScene(getClass().getCanonicalName());

		int yPos = 0;

		String equippedName = "";

		if (updateSlotImage()) {
			equippedName = ": " + Accessory.equipped().name();
		}

		//"Equipped Accessory" slot
		//Title
		Text slotTitle = PixelScene.createMultiline(Game.getVar(R.string.WndHats_SlotTitle) + equippedName, GuiProperties.titleFontSize());
		slotTitle.hardlight(0xFFFFFF);
		slotTitle.maxWidth(WIDTH - MARGIN * 2);
		slotTitle.measure();
		slotTitle.x = (WIDTH - slotTitle.width()) / 2;
		slotTitle.y = MARGIN;
		add(slotTitle);

		//Image
		slot.setPos(MARGIN, slotTitle.height() + MARGIN * 2);
		add(slot);

		//Unequip Button
		TextButton sb = new RedButton(Game.getVar(R.string.WndHats_UnequipButton)) {
			@Override
			protected void onClick() {
				super.onClick();
				Accessory.unequip();
				onBackPressed();
				GameScene.show(new WndHats());
			}
		};

		sb.setRect(slot.x + slot.width() * 2 + MARGIN, slot.y, slot.width() * 2, slot.height() / 2);

		add(sb);

		//List of Accessories
		//Title
		Text listTitle = PixelScene.createMultiline(Game.getVar(R.string.WndHats_ListTitle), GuiProperties.titleFontSize());
		listTitle.hardlight(TITLE_COLOR);
		listTitle.maxWidth(WIDTH - MARGIN * 2);
		listTitle.measure();
		listTitle.x = (WIDTH - listTitle.width()) / 2;
		listTitle.y = slot.y + slot.height() + MARGIN * 2;

		add(listTitle);

		List<String> hats = Accessory.getAccessoriesList();

		Component content = new Component();

		//List
		for (final String item : hats) {
			String price = Iap.getSkuPrice(item);
			Accessory accessory = Accessory.getByName(item);

			if (accessory.haveIt()) {
				price = Game.getVar(R.string.WndHats_Purchased);
			}

			//Image
			Image hat = accessory.getImage();
			hat.setPos(MARGIN, yPos);
			content.add(hat);

			//Text
			String hatName = Accessory.getByName(item).name();

			Text name = PixelScene.createMultiline(hatName, GuiProperties.regularFontSize());

			name.hardlight(0xFFFFFF);

			name.y = hat.y + MARGIN;
			name.maxWidth(WIDTH - MARGIN);
			name.measure();
			name.x = hat.x + hat.width() + MARGIN;

			content.add(name);
			float rbY = name.bottom() + MARGIN * 2;

			if (price != null) {
				//Pricetag
				SystemText priceTag = new SystemText(GuiProperties.mediumTitleFontSize());
				priceTag.text(price);

				priceTag.hardlight(0xFFFF00);
				priceTag.y = name.bottom() + MARGIN;
				priceTag.maxWidth((int) hat.width());
				priceTag.measure();
				priceTag.x = name.x;

				content.add(priceTag);

				//rbY = priceTag.bottom() + MARGIN;
			}

			String buttonText = Game.getVar(R.string.WndHats_InfoButton);
			final Accessory finalAccessory = accessory;

			if (accessory.haveIt()) {
				buttonText = Game.getVar(R.string.WndHats_EquipButton);
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
						Dungeon.hero.updateLook();
						onBackPressed();
						return;
					}
					GameScene.show(new WndHatInfo(item, finalPrice, currentWindow));
				}
			};

			rb.setRect(slot.x + slot.width() * 2 + MARGIN, rbY, slot.width() * 2, slot.height() / 2);
			//rb.setRect(WIDTH / 4, rbY, WIDTH / 2, BUTTON_HEIGHT);

			content.add(rb);
			yPos = (int) (rb.bottom() + MARGIN * 2);
		}

		int HEIGHT = PixelDungeon.landscape() ? HEIGHT_LANDSCAPE : HEIGHT_PORTRAIT;
		int h = Math.min(HEIGHT - MARGIN, yPos);

		float topGap = listTitle.y + listTitle.height() + MARGIN;
		float BottomGap = slotTitle.height() + slot.height() + listTitle.height() + MARGIN * 5;

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