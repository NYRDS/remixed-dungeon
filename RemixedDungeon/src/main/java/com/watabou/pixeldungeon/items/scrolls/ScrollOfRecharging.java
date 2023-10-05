
package com.watabou.pixeldungeon.items.scrolls;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Invisibility;
import com.watabou.pixeldungeon.effects.SpellSprite;
import com.watabou.pixeldungeon.effects.particles.EnergyParticle;
import com.watabou.pixeldungeon.utils.GLog;

import org.jetbrains.annotations.NotNull;

public class ScrollOfRecharging extends Scroll {
	
	@Override
	protected void doRead(@NotNull Char reader) {

		int count = reader.getBelongings().charge( true );
		charge( reader );
		
		Sample.INSTANCE.play( Assets.SND_READ );
		Invisibility.dispel(reader);
		
		if (count > 0) {
            GLog.i((count > 1 ? StringsManager.getVar(R.string.ScrollOfRecharging_Info1b)
					          : StringsManager.getVar(R.string.ScrollOfRecharging_Info1a)) );
			SpellSprite.show( reader, SpellSprite.CHARGE );
		} else {
            GLog.i(StringsManager.getVar(R.string.ScrollOfRecharging_Info2));
		}
		setKnown();

		reader.spend( TIME_TO_READ );
	}

	public static void charge(Char hero ) {
		hero.getSprite().centerEmitter().burst( EnergyParticle.FACTORY, 15 );
	}
	
	@Override
	public int price() {
		return isKnown() ? 40 * quantity() : super.price();
	}
}
