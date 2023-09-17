
package com.watabou.pixeldungeon.effects;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.audio.Sample;
import com.watabou.noosa.Gizmo;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.CharSprite;

public class IceBlock extends Gizmo {
	
	private float phase; 
	
	private final CharSprite target;
	
	public IceBlock( CharSprite target ) {
		this.target = target;
		phase = 0;
	}
	
	@Override
	public void update() {
		super.update();

		if ((phase += GameLoop.elapsed * 2) < 1) {
			target.tint( 0.83f, 1.17f, 1.33f, phase * 0.6f );
		} else {
			target.tint( 0.83f, 1.17f, 1.33f, 0.6f );
		}
	}
	
	public void melt() {

		target.resetColor();
		killAndErase();

		if (getVisible()) {
			Splash.at( target.center(), 0xFFB2D6FF, 5 );
			Sample.INSTANCE.play( Assets.SND_SHATTER );
		}
	}
	
	public static IceBlock freeze( CharSprite sprite ) {

		IceBlock iceBlock = new IceBlock( sprite );

		GameScene.addToMobLayer( iceBlock );

		return iceBlock;
	}
}
