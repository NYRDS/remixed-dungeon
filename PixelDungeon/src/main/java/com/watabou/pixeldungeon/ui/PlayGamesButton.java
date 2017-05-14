package com.watabou.pixeldungeon.ui;

import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Button;
import com.watabou.pixeldungeon.Preferences;

public class PlayGamesButton extends Button {

	private Image image;

	public PlayGamesButton() {
		super();
		
		width = image.width;
		height = image.height;
	}
	
	@Override
	protected void createChildren() {
		super.createChildren();
		
		image = Icons.get(Icons.TARGET);

		if(Preferences.INSTANCE.getBoolean(Preferences.KEY_USE_PLAY_GAMES,false)) {
			image.brightness(1.5f);
		} else {
			image.brightness(0.5f);
		}

		add( image );
	}
	
	@Override
	protected void layout() {
		super.layout();

		image.x = x;
		image.y = y;
	}

	@Override
	protected void onClick() {
		Game.scene().add(new WndPlayGames());
	}
}
