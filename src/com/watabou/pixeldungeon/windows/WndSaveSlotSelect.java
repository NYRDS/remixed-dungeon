package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.GameWithGoogleIap;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.SaveUtils;
import com.watabou.pixeldungeon.ui.DonateButton;

public class WndSaveSlotSelect extends WndOptionsColumns implements GameWithGoogleIap.IntersitialPoint {

	private boolean saving;
	private String slot;

	WndSaveSlotSelect(boolean _saving) {
		super(Game.getVar(R.string.WndSaveSlotSelect_SelectSlot), windowText(), slotInfos());
		saving = _saving;

		if (!saving) {
			for (int i = 0; i < 10; i++) {
				if (!SaveUtils.slotUsed(slotNameFromIndex(i), Dungeon.heroClass)) {
					setEnabled(i, false);
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

	private static String windowText() {
		if (PixelDungeon.donated() == 0 && PixelDungeon.canDonate()) {
			return Game.getVar(R.string.WndSaveSlotSelect_dontLike);
		}
		return "";
	}

	private static String slotNameFromIndex(int i) {
		return Integer.toString(i + 1);
	}

	private static String[] slotInfos() {
		String[] ret = new String[10];

		for (int i = 0; i < ret.length; i++) {
			ret[i] = SaveUtils.slotInfo(slotNameFromIndex(i), Dungeon.heroClass);
		}

		return ret;
	}

	@Override
	protected void onSelect(int index) {
		slot = slotNameFromIndex(index);

		final GameWithGoogleIap.IntersitialPoint returnTo = this;

		if (saving) {
			try {
				Dungeon.saveAll();
				SaveUtils.copySaveToSlot(slot, Dungeon.heroClass);

			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		Game.paused = true;
		
		if (PixelDungeon.donated() < 1) {
			PixelDungeon.displaySaveAndLoadAd(returnTo);
		} else {
			returnToWork();
		}
	}

	@Override
	public void returnToWork() {
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
