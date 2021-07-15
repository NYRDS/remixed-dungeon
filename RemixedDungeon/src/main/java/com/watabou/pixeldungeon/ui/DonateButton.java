package com.watabou.pixeldungeon.ui;

import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.noosa.Group;
import com.watabou.pixeldungeon.windows.WndDonate;

public class DonateButton extends ImageButton {

	private final Group parentWnd;

	public DonateButton(Group wnd) {
		super(Icons.SUPPORT.get());
		parentWnd = wnd;
		updateImage();
	}

	private void updateImage() {
		
		if(image != null) {
			remove(image);
		}
		
		switch (GamePreferences.donated()) {
		default:
		case 0:
			image = Icons.SUPPORT.get();
			break;
		case 1:
			image = Icons.CHEST_SILVER.get();
			break;
		case 2:
			image = Icons.CHEST_GOLD.get();
			break;
		case 3:
			image = Icons.CHEST_RUBY.get();
			break;
		case 4:
			image = Icons.CHEST_ROYAL.get();

		}
		
		add(image);
		layout();
	}

	public String getText() {
		switch (GamePreferences.donated()) {
		case 1:
		case 2:
		case 3:
		case 4:
            return StringsManager.getVar(R.string.DonateButton_thanks);
            default:
                return StringsManager.getVar(R.string.DonateButton_pleaseDonate);
        }
	}

	@Override
	protected void onClick() {
		parentWnd.add(new WndDonate());
	}
}
