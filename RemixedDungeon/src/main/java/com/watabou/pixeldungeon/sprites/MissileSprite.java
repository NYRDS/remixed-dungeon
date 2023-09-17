
package com.watabou.pixeldungeon.sprites;

import com.nyrds.pixeldungeon.effects.ZapEffect;
import com.watabou.noosa.tweeners.PosTweener;
import com.watabou.noosa.tweeners.Tweener;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;

public class MissileSprite extends ItemSprite implements Tweener.Listener {

	private Callback callback;
	
	public MissileSprite() {
		super();
		originToCenter();
	}

	public void reset(int from, int to, Item item, Callback listener) {
		revive();

		float scale = item.heapScale();
		setScaleXY(scale, scale);
		view(item);

		this.callback = listener;

		point( DungeonTilemap.tileToWorld( from ) );

		PointF dest = DungeonTilemap.tileToWorld( to );

		PointF d = PointF.diff( dest, point() ); 
		speed.set( d ).normalize().scale( ZapEffect.SPEED );

		if (item.isFliesStraight()) {
			angularSpeed = 0;
			setAngle(135 - (float)(Math.atan2( d.x, d.y ) / Math.PI * 180));
		} else {
			angularSpeed = item.isFliesFastRotating() ? 1440 : 720;
		}

		PosTweener tweener = new PosTweener( this, dest, d.length() / ZapEffect.SPEED );
		tweener.listener = this;
		getParent().add( tweener );
	}

	@Override
	public void onComplete( Tweener tweener ) {
		kill();
		if (callback != null) {
			callback.call();
		}
	}
}
