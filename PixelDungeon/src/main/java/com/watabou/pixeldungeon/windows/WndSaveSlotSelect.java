package com.watabou.pixeldungeon.windows;

import com.nyrds.android.util.GuiProperties;
import com.nyrds.android.util.ModdingMode;
import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.support.Ads;
import com.watabou.noosa.Game;
import com.watabou.noosa.InterstitialPoint;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.SaveUtils;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.DonateButton;
import com.watabou.pixeldungeon.ui.Icons;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.SimpleButton;
import com.watabou.pixeldungeon.ui.TextButton;
import com.watabou.pixeldungeon.ui.Window;

import java.util.ArrayList;

public class WndSaveSlotSelect extends Window implements InterstitialPoint {

	private static final int WIDTH         = 120;
	private static final int BUTTON_WIDTH  = 58;

	private static final String EMPTY_STRING = "";

	private boolean saving;
	private String  slot;

	WndSaveSlotSelect(final boolean _saving) {
		String options[] = slotInfos();

		Text tfTitle = PixelScene.createMultiline(Game.getVar(R.string.WndSaveSlotSelect_SelectSlot), GuiProperties.titleFontSize());
		tfTitle.hardlight(TITLE_COLOR);
		tfTitle.x = tfTitle.y = GAP;
		tfTitle.maxWidth(WIDTH - GAP * 2);
		tfTitle.measure();
		add(tfTitle);

		Text tfMesage = PixelScene.createMultiline(windowText(), GuiProperties.regularFontSize());
		tfMesage.maxWidth(WIDTH - GAP * 2);
		tfMesage.measure();
		tfMesage.x = GAP;
		tfMesage.y = tfTitle.y + tfTitle.height() + GAP;
		add(tfMesage);

		float pos = tfMesage.y + tfMesage.height() + GAP;

		ArrayList<TextButton> buttons = new ArrayList<>();
		for (int i = 0; i < options.length / 2 + 1; i++) {
			for (int j = 0; j < 2; j++) {
				final int index = i * 2 + j;
				if (!(index < options.length)) {
					break;
				}

				float additionalMargin = 0;
				float x = GAP + j * (BUTTON_WIDTH + GAP);

				final RedButton btn = new RedButton(options[index]) {
					@Override
					protected void onClick() {
						hide();
						onSelect(index);
					}
				};
				buttons.add(btn);

				if (!options[index].isEmpty()) {
					SimpleButton deleteBtn = new SimpleButton(Icons.get(Icons.CLOSE)) {
						protected void onClick() {
							final int slotIndex = index;
							WndOptions reallyDelete = new WndOptions(Game.getVar(R.string.WndSaveSlotSelect_Delete_Title), "",
									Game.getVar(R.string.WndSaveSlotSelect_Delete_Yes),
									Game.getVar(R.string.WndSaveSlotSelect_Delete_No)) {
								@Override
								protected void onSelect(int index) {
									if(index==0) {
										SaveUtils.deleteSaveFromSlot(slotNameFromIndexAndMod(slotIndex), Dungeon.heroClass);
										WndSaveSlotSelect.this.hide();
										GameScene.show(new WndSaveSlotSelect(_saving));
									}
								}
							};
							GameScene.show(reallyDelete);
						}
					};
					deleteBtn.setPos(x + BUTTON_WIDTH - deleteBtn.width() - GAP, pos);
					additionalMargin = deleteBtn.width() + GAP;
					add(deleteBtn);
				}

				btn.setRect(x, pos, BUTTON_WIDTH - additionalMargin - GAP, BUTTON_HEIGHT);
				add(btn);
			}
			pos += BUTTON_HEIGHT + GAP;
		}

		resize(WIDTH, (int) pos);


		saving = _saving;

		if (!saving) {
			for (int i = 0; i < 10; i++) {
				if (!isSlotIndexUsed(i)) {
					buttons.get(i).enable(false);
				}
			}
		}

		if (PixelDungeon.donated() == 0 && PixelDungeon.canDonate()) {
			DonateButton btn = new DonateButton();
			add(btn);
			btn.setPos(width / 2 - btn.width() / 2, height);
			resize(width, (int) (height + btn.height()));
		}
	}

	private static boolean isSlotIndexUsed(int index) {
		return SaveUtils.slotUsed(slotNameFromIndex(index), Dungeon.heroClass)
				|| SaveUtils.slotUsed(slotNameFromIndexAndMod(index), Dungeon.heroClass);
	}

	private static String getSlotToLoad(int index) {
		String slot = slotNameFromIndexAndMod(index);
		if (SaveUtils.slotUsed(slot, Dungeon.heroClass)) {
			return slot;
		} else {
			return slotNameFromIndex(index);
		}
	}

	private static String windowText() {
		if (PixelDungeon.donated() == 0 && PixelDungeon.canDonate()) {
			return Game.getVar(R.string.WndSaveSlotSelect_dontLike);
		}
		return EMPTY_STRING;
	}

	private static String slotNameFromIndex(int i) {
		return Integer.toString(i + 1);
	}

	private static String slotNameFromIndexAndMod(int i) {
		return ModdingMode.activeMod() + "_" + slotNameFromIndex(i);
	}

	private static String[] slotInfos() {
		String[] ret = new String[10];

		for (int i = 0; i < ret.length; i++) {
			ret[i] = SaveUtils.slotInfo(getSlotToLoad(i), Dungeon.heroClass);
		}

		return ret;
	}

	protected void onSelect(int index) {
		final InterstitialPoint returnTo = this;

		if (saving) {
			try {
				Dungeon.saveAll();
				slot = slotNameFromIndexAndMod(index);
				SaveUtils.copySaveToSlot(slot, Dungeon.heroClass);

			} catch (Exception e) {
				throw new TrackedRuntimeException(e);
			}
		}

		Game.paused = true;

		slot = getSlotToLoad(index);

		if (PixelDungeon.donated() < 1) {
			Ads.displaySaveAndLoadAd(returnTo);
		} else {
			returnToWork(true);
		}
	}

	@Override
	public void returnToWork(boolean res) {
		Game.executeInGlThread(new Runnable() {
			@Override
			public void run() {
				Game.paused = false;

				if (!saving) {
					SaveUtils.loadGame(slot, Dungeon.hero.heroClass);
				}
			}
		});
	}

}
