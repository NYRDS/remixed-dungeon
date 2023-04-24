package com.nyrds.pixeldungeon.items.common;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.noosa.particles.Emitter;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.effects.MagicMissile;
import com.watabou.pixeldungeon.effects.particles.PurpleParticle;
import com.watabou.pixeldungeon.items.wands.Wand;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import org.jetbrains.annotations.Nullable;

public class WandOfShadowbolt extends Wand {

	public WandOfShadowbolt() {
		imageFile = "items/wands.png";
		image = 13;
	}

	@Override
	protected void onZap( int cell, Char ch ) {
		if (ch != null) {
			int level = effectiveLevel();

			ch.damage( Random.Int( 4 + level*2, 3 + level * 3 ), this );
			ch.getSprite().burst( 0x551A8B, level / 2 + 3 );
		}
	}
	
	protected void fx( int cell, Callback callback ) {
		MagicMissile.shadow( getOwner().getSprite().getParent(), getOwner().getPos(), cell, callback );
		Sample.INSTANCE.play( Assets.SND_ZAP );
	}
	
	@Override
	public String desc() {
        return StringsManager.getVar(R.string.WandOfShadowbolt_Info);
    }

	@Override
	public boolean isKnown() {
		return true;
	}

	@Nullable
	@Override
	public Emitter.Factory emitter() {
		return PurpleParticle.FACTORY;
	}

	@Override
	public float emitterInterval() {
		return 0.1f;
	}
}
