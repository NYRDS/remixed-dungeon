

package com.watabou.noosa.tweeners;

import com.watabou.noosa.Visual;
import com.watabou.utils.PointF;

public class ScaleTweener extends Tweener {

	public final Visual visual;

	public final PointF start;
	public final PointF end;
	
	public ScaleTweener( Visual visual, PointF scale, float time ) {
		super( visual, time );
		
		this.visual = visual;
		start = visual.scale;
		end = scale;
	}

	@Override
	protected void updateValues( float progress ) {
		visual.Scale(PointF.inter( start, end, progress ));
	}
}
