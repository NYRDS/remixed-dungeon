
package com.watabou.pixeldungeon.sprites;

import com.nyrds.pixeldungeon.ai.Sleeping;
import com.watabou.noosa.Animation;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.scenes.GameScene;

public class MobSprite extends CharSprite {

	private static final float FADE_TIME	= 3f;
	// a carcass lies here already - no point in a long corpse fade
	private static final float CARCASS_FADE_TIME = 1f;

	@Override
	public void update() {
		ch.ifPresent( chr -> {
			if (chr instanceof Mob) {
				Mob mob = (Mob) chr;
				sleeping = mob.getState() instanceof Sleeping;
				controlled = mob.isPet();
			}
		});
		super.update();
	}

	@Override
	public void onComplete( Animation anim ) {

		super.onComplete( anim );

		if (anim == die) {
			float fadeTime = FADE_TIME;
			if (ch.isPresent() && ch.get() instanceof Mob && ((Mob) ch.get()).droppedCarcass()) {
				fadeTime = CARCASS_FADE_TIME;
			}
			GameScene.addToMobLayer(new AlphaTweener(this, 0, fadeTime) {
					@Override
					protected void onComplete() {
						MobSprite.this.killAndErase();
					}
				});
		}
	}

}
