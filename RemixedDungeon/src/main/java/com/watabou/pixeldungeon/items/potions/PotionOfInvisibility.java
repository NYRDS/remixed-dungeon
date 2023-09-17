
package com.watabou.pixeldungeon.items.potions;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Invisibility;
import com.watabou.pixeldungeon.items.scrolls.BlankScroll;
import com.watabou.pixeldungeon.items.scrolls.Scroll;
import com.watabou.pixeldungeon.utils.GLog;

public class PotionOfInvisibility extends UpgradablePotion {

	{
		labelIndex =3;
	}

	@Override
	protected void apply(Char hero ) {
		setKnown();
		Buff.affect( hero, Invisibility.class, (float) (Invisibility.DURATION * qualityFactor()));
        GLog.i(StringsManager.getVar(R.string.PotionOfInvisibility_Apply));
		Sample.INSTANCE.play( Assets.SND_MELD );
	}
	
	@Override
	public String desc() {
        return StringsManager.getVar(R.string.PotionOfInvisibility_Info);
    }

	@Override
	public int basePrice() {
		return 40;
	}

	@Override
	protected void moistenScroll(Scroll scroll, Char owner) {
		int quantity = detachMoistenItems(scroll, (int) (3*qualityFactor()));

        GLog.i(StringsManager.getVar(R.string.Potion_RuneDissaperaed), scroll.name());
		
		moistenEffective(owner);
		
		BlankScroll moistenScroll = new BlankScroll();
		moistenScroll.quantity(quantity);
		owner.collect(moistenScroll);
	}
}
