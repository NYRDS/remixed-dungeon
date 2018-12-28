package com.nyrds.pixeldungeon.effects;

import com.watabou.noosa.Animation;
import com.watabou.noosa.CompositeMovieClip;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;

public class CustomClipEffect extends CompositeMovieClip implements MovieClip.Listener {

	private TextureFilm frames;
	private Callback    onAnimComplete;
	private PointF      centerShift;

	public CustomClipEffect(Object texture, int xs, int ys) {
		texture(texture);

		frames = new TextureFilm(texture,xs , ys);
		centerShift = new PointF(-(xs - DungeonTilemap.SIZE) / 2, -(ys-DungeonTilemap.SIZE) / 2);
		origin.set(xs / 2, ys / 2);
	}

	public void place(int cell) {
		PointF p = DungeonTilemap.tileToWorld(cell);
		x = p.x + centerShift.x;
		y = p.y + centerShift.y;
	}

	@Override
	public void update() {
		super.update();
	}

	public void setAnim(int fps, boolean looped, Callback animComplete, int... framesSeq) {
		curAnim = new Animation(fps, looped);
		curAnim.frames(frames, framesSeq);
		onAnimComplete = animComplete;
		listener = this;
	}

	public void playAnim(Animation anim,Callback animComplete) {
		onAnimComplete = animComplete;
		listener = this;
		play(anim, true);
	}

	@Override
	public void onComplete(Animation anim) {
		if (onAnimComplete != null) {
			onAnimComplete.call();
			onAnimComplete = null;
		}
	}
}
