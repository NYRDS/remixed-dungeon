package com.watabou.pixeldungeon.ui;

import com.watabou.pixeldungeon.RemixedDungeon;
import com.watabou.pixeldungeon.scenes.WelcomeScene;

public class ChangelogButton extends ImageButton {

	public ChangelogButton() {
		super(Icons.get(Icons.NYRDIE));
	}

	@Override
	protected void onClick() {
		RemixedDungeon.switchScene(WelcomeScene.class);
	}
}
