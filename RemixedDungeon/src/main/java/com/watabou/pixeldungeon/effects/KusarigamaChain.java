
package com.watabou.pixeldungeon.effects;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.audio.Sample;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.Assets;
import com.watabou.utils.PointF;

public class KusarigamaChain extends Image {

	private static final double A = 180 / Math.PI;
	
	private static final float DURATION	= 0.5f;
	
	private float timeLeft;
	
	public KusarigamaChain( PointF s, PointF e ) {
		super( Effects.get( Effects.Type.CHAIN ) );

		setOrigin( 0, height / 2 );

		setX(s.x - origin.x);
		setY(s.y - origin.y);
		
		float dx = e.x - s.x;
		float dy = e.y - s.y;
		setAngle((float)(Math.atan2( dy, dx ) * A));
        setScaleX((float)Math.sqrt( dx * dx + dy * dy ) / width);
		
		Sample.INSTANCE.play( Assets.SND_ROTTEN_DROP );
		
		timeLeft = DURATION;
	}
	
	@Override
	public void update() {
		super.update();
		
		float p = timeLeft / DURATION;
		alpha( p );
		setScaleY( p );
		
		if ((timeLeft -= GameLoop.elapsed) <= 0) {
			killAndErase();
		}
	}

}
