package com.watabou.pixeldungeon.ui;

import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Button;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.scenes.AllowStatisticsCollectionScene;

public class StaticsticsButton extends Button {

	private Image image;

	public StaticsticsButton() {
		super();
		
		width = image.width;
		height = image.height;
	}
	
	@Override
	protected void createChildren() {
		super.createChildren();
		
		image = Icons.get(Icons.GRAPHS);
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
		PixelDungeon.switchScene(AllowStatisticsCollectionScene.class);
	}
}
