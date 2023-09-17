
package com.watabou.pixeldungeon.ui;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.game.RemixedDungeon;
import com.watabou.pixeldungeon.scenes.TitleScene;

public class ExitButton extends ImageButton {

	public ExitButton() {
		super(Icons.EXIT.get());
	}

	@Override
	protected void onClick() {
		if (GameLoop.scene() instanceof TitleScene) {
			Game.shutdown();
		} else {
			RemixedDungeon.switchNoFade( TitleScene.class );
		}
	}
}
