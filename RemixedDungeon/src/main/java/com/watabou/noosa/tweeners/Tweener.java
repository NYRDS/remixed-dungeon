

package com.watabou.noosa.tweeners;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.watabou.noosa.Gizmo;

abstract public class Tweener extends Gizmo {

	public final Gizmo target;

	public final float interval;
	public float elapsed;
	
	public Listener listener;
	
	public Tweener( Gizmo target, float interval ) {
		super();
		
		this.target = target;
		this.interval = interval;
		
		elapsed = 0;
	}
	
	@Override
	public void update() {
		elapsed += GameLoop.elapsed;
		if (elapsed >= interval) {
			updateValues( 1 );
			onComplete();
			killAndErase();
		} else {
			updateValues( elapsed / interval );
		}
	}
	
	protected void onComplete() {
		if (listener != null) {
			listener.onComplete( this );
		}
	}
	
	abstract protected void updateValues( float progress );
	
	public interface Listener {
		void onComplete( Tweener tweener );
	}
}
