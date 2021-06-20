package com.watabou.pixeldungeon.ui;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.game.Game;
import com.watabou.utils.SystemTime;

public class PlayGamesButton extends ImageButton {

	private long lastUpdatedTime;

	public PlayGamesButton() {
		super(Icons.get(Icons.PLAY_GAMES));
		updateStatus();
	}

	private void updateStatus() {
		if (Game.instance().playGames.isConnected()) {
			image.brightness(1.5f);
		} else {
			image.brightness(0.5f);
		}
	}

	@Override
	public void update() {
		super.update();
		if(SystemTime.now() - lastUpdatedTime > 1000) {
			lastUpdatedTime = SystemTime.now();
			updateStatus();
		}
	}

	@Override
	protected void onClick() {
		GameLoop.addToScene(new WndPlayGames());
	}
}
