package com.watabou.pixeldungeon.effects;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.gl.Gl;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.utils.PointF;

public class DeathRay extends Image {

	private static final double A = 180 / Math.PI;

	private static final float DURATION	= 0.5f;

	private float timeLeft;

	public DeathRay(int from, int to) {
		super( Effects.get( Effects.Type.RAY ) );

		PointF s = DungeonTilemap.tileCenterToWorld(from);
		PointF e = DungeonTilemap.tileCenterToWorld(to);

		origin.set( 0, height / 2 );

		x = s.x - origin.x;
		y = s.y - origin.y;

		float dx = e.x - s.x;
		float dy = e.y - s.y;
		angle = (float)(Math.atan2( dy, dx ) * A);
		scale.x = (float)Math.sqrt( dx * dx + dy * dy ) / width;

		Sample.INSTANCE.play( Assets.SND_RAY );

		timeLeft = DURATION;
	}

	@Override
	public void update() {
		super.update();

		float p = timeLeft / DURATION;
		alpha( p );
		scale.set( scale.x, p );

		if ((timeLeft -= GameLoop.elapsed) <= 0) {
			killAndErase();
		}
	}

	@Override
	public void draw() {
		Gl.blendSrcAlphaOne();
		super.draw();
		Gl.blendSrcAlphaOneMinusAlpha();
	}
}
