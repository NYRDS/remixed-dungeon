package com.nyrds.pixeldungeon.items.icecaves;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.ResultDescriptions;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.Fire;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.actors.buffs.Slow;
import com.watabou.pixeldungeon.effects.MagicMissile;
import com.watabou.pixeldungeon.effects.particles.FlameParticle;
import com.watabou.pixeldungeon.effects.particles.SnowParticle;
import com.watabou.pixeldungeon.items.wands.SimpleWand;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

public class WandOfIcebolt extends SimpleWand  {

	@Override
	protected void onZap( int cell ) {

		Char ch = Actor.findChar( cell );
		if (ch != null) {

			int level = effectiveLevel();

			ch.damage( Random.Int( 3, 5 + level * 2 ), this );
			ch.getSprite().burst( 0xFF99FFFF, level / 2 + 3 );
			Buff.affect( ch, Slow.class, Slow.duration( ch ) / 2 + effectiveLevel() );

			if (ch == getCurUser() && !ch.isAlive()) {
				Dungeon.fail( Utils.format( ResultDescriptions.WAND, name, Dungeon.depth ) );
				GLog.n(Game.getVar(R.string.WandOfMagicMissile_Info1));
			}
		}
	}
	
	protected void fx( int cell, Callback callback ) {
		MagicMissile.ice( wandUser.getSprite().getParent(), wandUser.getPos(), cell, callback );
		Sample.INSTANCE.play( Assets.SND_ZAP );
	}
	
	@Override
	public String desc() {
		return Game.getVar(R.string.WandOfFirebolt_Info);
	}
}
