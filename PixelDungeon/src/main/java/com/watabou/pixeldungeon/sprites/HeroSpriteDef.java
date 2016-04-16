package com.watabou.pixeldungeon.sprites;

import com.watabou.noosa.Animation;
import com.watabou.noosa.Camera;
import com.watabou.noosa.tweeners.Tweener;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.items.armor.Armor;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.utils.Callback;

/**
 * Created by mike on 16.04.2016.
 */
public class HeroSpriteDef extends MobSpriteDef {

	public HeroSpriteDef(String defName, int kind) {
		super(defName, kind);
	}

	private Animation fly;

	private Tweener  jumpTweener;
	private Callback jumpCallback;

	@Override
	public void place(int p) {
		super.place(p);
		Camera.main.target = this;
	}

	@Override
	public void move(int from, int to) {
		super.move(from, to);
		if (ch.flying) {
			play(fly);
		}
		Camera.main.target = this;
	}

	public void updateArmor(Armor armor) {

	}

	public void jump(int from, int to, Callback callback) {
		jumpCallback = callback;

		int distance = Dungeon.level.distance(from, to);
		jumpTweener = new JumpTweener(this, worldToCamera(to), distance * 4,
				distance * 0.1f);
		jumpTweener.listener = this;
		getParent().add(jumpTweener);

		turnTo(from, to);
		play(fly);
	}

	@Override
	public void onComplete(Tweener tweener) {
		if (tweener == jumpTweener) {

			if (getVisible() && Dungeon.level.water[ch.getPos()] && !ch.flying) {
				GameScene.ripple(ch.getPos());
			}
			if (jumpCallback != null) {
				jumpCallback.call();
			}

		} else {
			super.onComplete(tweener);
		}
	}


}
