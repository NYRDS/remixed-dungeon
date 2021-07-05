package com.watabou.pixeldungeon.ui;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.watabou.pixeldungeon.scenes.AllowStatisticsCollectionScene;

public class StatisticsButton extends ImageButton {

	public StatisticsButton() {
		super(Icons.get(Icons.GRAPHS));
	}

	@Override
	protected void onClick() {
		GameLoop.switchScene(AllowStatisticsCollectionScene.class);
	}
}
