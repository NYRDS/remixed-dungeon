
package com.watabou.pixeldungeon.items.scrolls;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Sleep;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;

public class ScrollOfLullaby extends Scroll {
	
	@Override
	protected void doRead(@NotNull Char reader) {

		reader.getSprite().centerEmitter().start( Speck.factory( Speck.NOTE ), 0.3f, 5 );
		Sample.INSTANCE.play( Assets.SND_LULLABY );
		
		int count = 0;
		Mob affected = null;
		Level level = reader.level();
		for (Mob mob : level.getCopyOfMobsArray()) {
			if (level.fieldOfView[mob.getPos()]) {
				Buff.affect( mob, Sleep.class );
				if (mob.hasBuff( Sleep.class )) {
					affected = mob;
					count++;
				}
			}
		}
		
		switch (count) {
		case 0:
            GLog.i(StringsManager.getVar(R.string.ScrollOfLullaby_Info1));
			break;
		case 1:
            GLog.i(Utils.format(R.string.ScrollOfLullaby_Info2, affected.getName()));
			break;
		default:
            GLog.i(StringsManager.getVar(R.string.ScrollOfLullaby_Info3));
		}
		setKnown();

		reader.spend( TIME_TO_READ );
	}

	@Override
	public int price() {
		return isKnown() ? 50 * quantity() : super.price();
	}
}
