package com.nyrds.retrodungeon.items.icecaves;

import android.support.annotation.Nullable;

import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.ResultDescriptions;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Frost;
import com.watabou.pixeldungeon.actors.buffs.Slow;
import com.watabou.pixeldungeon.effects.MagicMissile;
import com.watabou.pixeldungeon.effects.particles.SnowParticle;
import com.watabou.pixeldungeon.items.wands.Wand;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

public class WandOfIcebolt extends Wand {

	public WandOfIcebolt() {
		imageFile = "items/wands.png";
		image = 12;
	}

	@Override
	protected void onZap( int cell ) {

		Char ch = Actor.findChar( cell );
		if (ch != null) {

			int level = effectiveLevel();

			ch.damage( Random.Int( 3 + level, 4 + level * 2 ), this );
			ch.getSprite().burst( 0xFF99FFFF, level / 2 + 3 );

			Buff.affect( ch, Frost.class, Frost.duration( ch ) / 2 + effectiveLevel() );
			Buff.affect( ch, Slow.class, Slow.duration( ch ) / 2 + effectiveLevel() );

			if (ch == getCurUser() && !ch.isAlive()) {
				Dungeon.fail( Utils.format( ResultDescriptions.WAND, name, Dungeon.depth ) );
				GLog.n(Game.getVar(R.string.WandOfIcebolt_Info1));
			}
		}
	}
	
	protected void fx( int cell, Callback callback ) {
		MagicMissile.ice( wandUser.getSprite().getParent(), wandUser.getPos(), cell, callback );
		Sample.INSTANCE.play( Assets.SND_ZAP );
	}
	
	@Override
	public String desc() {
		return Game.getVar(R.string.WandOfIcebolt_Info);
	}

	@Override
	public boolean isKnown() {
		return true;
	}

	@Nullable
	@Override
	public Emitter.Factory emitter() {
		return SnowParticle.FACTORY;
	}

	@Override
	public float emitterInterval() {
		return 0.1f;
	}
}
