package com.watabou.pixeldungeon.ui;

import com.nyrds.pixeldungeon.support.EuConsent;
import com.watabou.noosa.Game;

public class ChangelogButton extends ImageButton {

	public ChangelogButton() {
		super(Icons.get(Icons.NYRDIE));
	}

	@Override
	protected void onClick() {
		EuConsent.check(Game.instance());
		//PixelDungeon.switchScene(WelcomeScene.class);
	}
}
