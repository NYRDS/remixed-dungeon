
package com.watabou.pixeldungeon.ui;

import com.watabou.pixeldungeon.windows.WndSettings;

public class PrefsButton extends ImageButton {
	

	public PrefsButton() {
		super(Icons.get(Icons.PREFS));
	}
	
	@Override
	protected void onClick() {
		getParent().add( new WndSettings() );
	}
}
