package com.watabou.pixeldungeon.ui;

import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.windows.WndPremiumSettings;

public class PremiumPrefsButton extends ImageButton {

	public PremiumPrefsButton() {
		super(Icons.SUPPORTED.get());
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
