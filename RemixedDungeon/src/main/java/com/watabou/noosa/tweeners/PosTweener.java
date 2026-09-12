

package com.watabou.noosa.tweeners;

import com.nyrds.LuaInterface;
import com.watabou.noosa.Group;
import com.watabou.noosa.Visual;
import com.watabou.utils.PointF;

public class PosTweener extends Tweener {

	public final Visual visual;

	public final PointF start;
	public final PointF end;

	@LuaInterface
	public static void attachTo(Visual visual, float dx, float dy, float time) {
		Group parent = visual.getParent();
		if (parent == null) {
			// headless or sprite not attached to a scene yet: no tween to run
			return;
		}
		PosTweener tweener = new PosTweener(visual,dx,dy,time);
		parent.add(tweener);
	}


	public PosTweener( Visual visual, float dx, float dy, float time ) {
		super( visual, time );

		this.visual = visual;
		start = visual.point();
		end = start.clone();
		end.offset(dx,dy);
	}

	public PosTweener( Visual visual, PointF pos, float time ) {
		super( visual, time );
		
		this.visual = visual;
		start = visual.point();
		end = pos;
	}

	@Override
	protected void updateValues( float progress ) {
		visual.point( PointF.inter( start, end, progress ) );
	}
}
