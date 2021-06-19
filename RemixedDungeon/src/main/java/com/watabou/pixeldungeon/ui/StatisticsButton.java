package com.watabou.pixeldungeon.ui;

import com.nyrds.platform.game.RemixedDungeon;
import com.watabou.pixeldungeon.scenes.AllowStatisticsCollectionScene;

public class StatisticsButton extends ImageButton {

	public StatisticsButton() {
		super(Icons.get(Icons.GRAPHS));
	}

	@Override
	protected void onClick() {
		RemixedDungeon.switchScene(AllowStatisticsCollectionScene.class);
	}
}
