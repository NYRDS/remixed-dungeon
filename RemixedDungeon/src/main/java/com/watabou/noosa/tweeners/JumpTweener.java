package com.watabou.noosa.tweeners;

import com.nyrds.LuaInterface;
import com.watabou.noosa.Visual;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.utils.PointF;

/**
 * Created by mike on 16.04.2016.
 */
public class JumpTweener extends Tweener {

	public final Visual visual;

	public final PointF start;
	public final PointF end;

	public final float height;

	@LuaInterface
	public static void attachTo(CharSprite spr, int targetCell,  float height, float time) {
		JumpTweener tweener = new JumpTweener(spr, spr.worldToCamera(targetCell), height, time);
		spr.getParent().add(tweener);
	}


	public JumpTweener(Visual visual, PointF pos, float height, float time) {
		super(visual, time);

		this.visual = visual;
		start = visual.point();
		end = pos;

		this.height = height;
	}

	@Override
	protected void updateValues(float progress) {
		visual.point(PointF.inter(start, end, progress).offset(0,
				-height * 4 * progress * (1 - progress)));
	}
}
