package com.nyrds.retrodungeon.levels.objects.sprites;

import com.nyrds.retrodungeon.levels.objects.LevelObject;
import com.watabou.noosa.Animation;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.tweeners.PosTweener;
import com.watabou.noosa.tweeners.ScaleTweener;
import com.watabou.noosa.tweeners.Tweener;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

public class LevelObjectSprite extends MovieClip implements Tweener.Listener, MovieClip.Listener {

	private TextureFilm frames;
	private Callback    onAnimComplete;
	private PointF      centerShift;

	public LevelObjectSprite() {}

	public void move(int from, int to) {
		if (getParent() != null) {
			Tweener motion = new PosTweener(this, DungeonTilemap.tileToWorld(to).offset(centerShift), 0.1f);
			motion.listener = this;
			getParent().add(motion);

			if (getVisible() && Dungeon.level.water[from]) {
				GameScene.ripple(from);
			}
		}
	}

	public void fall() {

		origin.set( width / 2, height - DungeonTilemap.SIZE / 2 );
		angularSpeed = Random.Int( 2 ) == 0 ? -720 : 720;

		getParent().add( new ScaleTweener( this, new PointF( 0, 0 ), 1f ) {
			@Override
			protected void onComplete() {
				LevelObjectSprite.this.killAndErase();
			}

			@Override
			protected void updateValues( float progress ) {
				super.updateValues( progress );
				am = 1 - progress;
			}
		} );
	}

	private void setLevelPos(int cell) {
		PointF p = DungeonTilemap.tileToWorld(cell);
		x = p.x + centerShift.x;
		y = p.y + centerShift.y;
	}

	public void reset(LevelObject object) {
		revive();

		texture(object.texture());

		int xs = object.getSpriteXS();
		int ys = object.getSpriteYS();

		frames = new TextureFilm(texture,xs , ys);
		centerShift = new PointF(-(xs - DungeonTilemap.SIZE) / 2, -(ys-DungeonTilemap.SIZE) / 2);
		origin.set(xs / 2, ys / 2);

		reset(object.image());
		alpha(1f);

		setLevelPos(object.getPos());
		setVisible(!object.secret());
	}

	public void reset(int image) {
		frame(frames.get(image));
	}

	@Override
	public void update() {
		super.update();
		//setVisible(true);
		//setVisible(pos == -1 || Dungeon.visible[pos]);
	}

	@Override
	public void onComplete(Tweener tweener) {

	}

	public void playAnim(int fps, boolean looped, Callback animComplete, int... framesSeq) {
		Animation anim = new Animation(fps, looped);
		anim.frames(frames, framesSeq);
		onAnimComplete = animComplete;
		listener = this;
		play(anim);
	}

	@Override
	public void onComplete(Animation anim) {
		if (onAnimComplete != null) {
			onAnimComplete.call();
			onAnimComplete = null;
		}
	}

}
