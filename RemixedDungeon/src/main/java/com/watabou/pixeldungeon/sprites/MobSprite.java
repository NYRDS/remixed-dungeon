
package com.watabou.pixeldungeon.sprites;

import com.nyrds.pixeldungeon.ai.Sleeping;
import com.watabou.noosa.Animation;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.scenes.GameScene;

public class MobSprite extends CharSprite {

	private static final float FADE_TIME	= 3f;

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
			GameScene.addToMobLayer(new AlphaTweener(this, 0, FADE_TIME) {
					@Override
					protected void onComplete() {
						MobSprite.this.killAndErase();
					}
				});
		}
	}

}
