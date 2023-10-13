
package com.watabou.pixeldungeon.items.potions;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

public class PotionOfExperience extends Potion {

	{
		labelIndex = 11;
	}

	@Override
	protected void apply(Char chr ) {
		setKnown();
		chr.earnExp(chr.expToLevel() - chr.getExp());
	}

	public void shatter( int cell ) {
		Sample.INSTANCE.play( Assets.SND_SHATTER );
		splash( cell );

		Char chr = Actor.findChar(cell);
		if (chr != null) {
			apply(chr);
			return;
		}
		GLog.i(Utils.format(R.string.Potion_Shatter, color()));
	}

	@Override
	public String desc() {
        return StringsManager.getVar(R.string.PotionOfExperience_Info);
    }
	
	@Override
	public int price() {
		return isKnown() ? 80 * quantity() : super.price();
	}
}
