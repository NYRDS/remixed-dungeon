package com.watabou.pixeldungeon.ui;

import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Button;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.windows.WndPremiumSettings;

public class PremiumPrefsButton extends Button {
	
	private Image image;
	
	public PremiumPrefsButton() {
		super();
		
		width = image.width;
		height = image.height;
	}
	
	@Override
	protected void createChildren() {
		super.createChildren();
		
		image = Icons.SUPPORTED.get();
		add( image );
	}
	
	@Override
	protected void layout() {
		super.layout();
		
		image.x = x;
		image.y = y;
	}
	
	@Override
	protected void onTouchDown() {
		image.brightness( 1.5f );
		Sample.INSTANCE.play( Assets.SND_CLICK );
	}
	
	@Override
	protected void onTouchUp() {
		image.resetColor();
	}
	
	@Override
	protected void onClick() {
		getParent().add( new WndPremiumSettings( ) );
	}
}
