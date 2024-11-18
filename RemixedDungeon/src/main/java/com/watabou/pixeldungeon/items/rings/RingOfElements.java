
package com.watabou.pixeldungeon.items.rings;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.guts.BurningFist;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.blobs.LiquidFlame;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.actors.buffs.Poison;
import com.watabou.pixeldungeon.actors.mobs.Eye;
import com.watabou.pixeldungeon.actors.mobs.Warlock;
import com.watabou.pixeldungeon.levels.traps.LightningTrap;
import com.watabou.utils.Random;

import java.util.HashSet;
import java.util.Set;

public class RingOfElements extends Ring {

	@Override
	public ArtifactBuff buff( ) {
		return new Resistance();
	}
	
	@Override
	public String desc() {
        return isKnown() ? StringsManager.getVar(R.string.RingOfElements_Info) : super.desc();
	}


	public class Resistance extends RingBuff {

		private final Set<String> FULL;
		{
			FULL = new HashSet<>();
			FULL.add( Burning.class.getSimpleName() );
			FULL.add( ToxicGas.class.getSimpleName() );
			FULL.add( Poison.class.getSimpleName() );
			FULL.add( LightningTrap.Electricity.class.getSimpleName() );
			FULL.add( Warlock.class.getSimpleName() );
			FULL.add( Eye.class.getSimpleName() );
			FULL.add( BurningFist.class.getSimpleName() );
			FULL.add( LiquidFlame.class.getSimpleName() );
		}


		public Set<String> resistances() {
			if (Random.Int( level() + 3 ) >= 3) {
				return FULL;
			} else {
				return EMPTY_STRING_SET;
			}
		}
		
		public float durationFactor() {
			return level() < 0 ? 1 : (2 + 0.5f * level()) / (2 + level());
		}
	}
}
