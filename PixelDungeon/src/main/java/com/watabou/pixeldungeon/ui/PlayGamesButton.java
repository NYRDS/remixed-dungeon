package com.watabou.pixeldungeon.ui;

import com.nyrds.retrodungeon.support.PlayGames;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Button;
import com.watabou.utils.SystemTime;

public class PlayGamesButton extends Button {

	private Image image;
	private long lastUpdatedTime;

	public PlayGamesButton() {
		super();
		
		width = image.width;
		height = image.height;
	}

	private void updateStatus() {
		if(PlayGames.isConnected()) {
			image.brightness(1.5f);
		} else {
			image.brightness(0.5f);
		}
	}

	@Override
	protected void createChildren() {
		super.createChildren();
		
		image = Icons.get(Icons.PLAY_GAMES);

		updateStatus();

		add( image );
	}
	
	@Override
	protected void layout() {
		super.layout();

		image.x = x;
		image.y = y;
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
		Game.scene().add(new WndPlayGames());
	}
}
