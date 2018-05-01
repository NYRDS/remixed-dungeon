package com.watabou.pixeldungeon.ui;

import com.watabou.pixeldungeon.windows.WndPremiumSettings;

public class PremiumPrefsButton extends ImageButton {

	public PremiumPrefsButton() {
		super(Icons.SUPPORTED.get());
	}

	@Override
	protected void onClick() {
		getParent().add( new WndPremiumSettings( ) );
	}
}
