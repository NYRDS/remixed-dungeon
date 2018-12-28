package com.watabou.pixeldungeon.ui;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.pixeldungeon.RemixedDungeon;
import com.watabou.pixeldungeon.windows.WndDonate;

public class DonateButton extends ImageButton {

	private Group parentWnd;

	public DonateButton(Group wnd) {
		super(Icons.SUPPORT.get());
		parentWnd = wnd;
		updateImage();
	}

	private void updateImage() {
		
		if(image != null) {
			remove(image);
		}
		
		switch (RemixedDungeon.donated()) {
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
		switch (RemixedDungeon.donated()) {
		case 1:
		case 2:
		case 3:
		case 4:
			return Game.getVar(R.string.DonateButton_thanks);
		default:
			return Game.getVar(R.string.DonateButton_pleaseDonate);
		}
	}

	@Override
	protected void onClick() {
		parentWnd.add(new WndDonate());
	}
}
