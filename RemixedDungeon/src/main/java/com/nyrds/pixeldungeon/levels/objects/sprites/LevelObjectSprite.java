package com.nyrds.pixeldungeon.levels.objects.sprites;

import com.nyrds.pixeldungeon.levels.objects.LevelObject;
import com.nyrds.util.ModError;
import com.watabou.noosa.Animation;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.tweeners.FallTweener;
import com.watabou.noosa.tweeners.PosTweener;
import com.watabou.noosa.tweeners.Tweener;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

import lombok.var;

public class LevelObjectSprite extends MovieClip implements Tweener.Listener, MovieClip.Listener {

	private TextureFilm frames;
	private Callback    onAnimComplete;
	private PointF      centerShift;

	public LevelObjectSprite()
	{}

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

		origin.set( width / 2, height - DungeonTilemap.SIZE / 2.f );
		angularSpeed = Random.Int( 2 ) == 0 ? -720 : 720;

		getParent().add(new FallTweener(this));
	}

	private void setLevelPos(int cell) {
		PointF p = DungeonTilemap.tileToWorld(cell);
		x = p.x + centerShift.x;
		y = p.y + centerShift.y;
	}

	public void reset(@NotNull LevelObject object) {
		revive();

		if(!object.ignoreIsometricShift()) {
			setIsometricShift(true);
		}

		texture(object.getTextureFile());

		int xs = object.getSpriteXS();
		int ys = object.getSpriteYS();

		frames = new TextureFilm(texture, xs, ys);
		centerShift = new PointF(-(xs - DungeonTilemap.SIZE) / 2.f, -(ys-DungeonTilemap.SIZE) / 2.f);
		origin.set(xs / 2.f, ys / 2.f);

		int image = object.image();
		var frame = frames.get(image);

		if (frame == null) {
			throw new ModError(Utils.format("bad index %d in image %s for %s", image, texture.toString(), object.getEntityKind()));
		}

		reset(image);
		alpha(1f);

		setLevelPos(object.getPos());
		setVisible(!object.secret());

		object.resetVisualState();
	}

	public void reset(int image) {
		frame(frames.get(image));
	}

	@Override
	public void onComplete(Tweener tweener) {

	}

	public void playAnim(Animation anim, Callback animComplete) {
		onAnimComplete = animComplete;
		listener = this;
		play(anim);
	}

	public void playAnim(int fps, boolean looped, Callback animComplete, int... framesSeq) {
		Animation anim = new Animation(fps, looped);
		anim.frames(frames, framesSeq);
		playAnim(anim, animComplete);
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
