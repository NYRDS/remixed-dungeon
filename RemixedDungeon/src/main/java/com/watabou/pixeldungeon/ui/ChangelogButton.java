package com.watabou.pixeldungeon.ui;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.watabou.pixeldungeon.scenes.WelcomeScene;

public class ChangelogButton extends ImageButton {

	public ChangelogButton() {
		super(Icons.get(Icons.NYRDIE));
	}

	@Override
	protected void onClick() {
		GameLoop.switchScene(WelcomeScene.class);
	}
}
