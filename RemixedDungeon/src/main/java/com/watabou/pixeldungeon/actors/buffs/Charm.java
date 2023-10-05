
package com.watabou.pixeldungeon.actors.buffs;

import com.nyrds.platform.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.quest.DriedRose;
import com.watabou.pixeldungeon.items.rings.RingOfElements.Resistance;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.ui.BuffIndicator;

import org.jetbrains.annotations.NotNull;

public class Charm extends FlavourBuff {
	
	@Override
	public boolean attachTo(@NotNull Char target ) {

		if(target.hasBuff(DriedRose.OneWayLoveBuff.class)){
			return false;
		}

		if (super.attachTo( target )) {
			if(GameScene.isSceneReady()) {
				target.getSprite().centerEmitter().start(Speck.factory(Speck.HEART), 0.2f, 5);
				Sample.INSTANCE.play(Assets.SND_CHARMS);
			}
			target.pacified = true;
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void detach() {
		target.pacified = false;
		super.detach();
	}
	
	@Override
	public int icon() {
		return BuffIndicator.HEART;
	}

	public static float durationFactor(@NotNull Char ch ) {

		if(ch.hasBuff(DriedRose.OneWayLoveBuff.class)) {
			return 0;
		}

		if(ch.hasBuff(DriedRose.OneWayCursedLoveBuff.class)) {
			return 2;
		}

		Resistance r = ch.buff( Resistance.class );
		return r != null ? r.durationFactor() : 1;
	}
}
