
package com.watabou.pixeldungeon.items.scrolls;
import com.nyrds.pixeldungeon.mechanics.buffs.BuffFactory;
import com.nyrds.platform.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.effects.particles.ShadowParticle;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.utils.Random;
import org.jetbrains.annotations.NotNull;

public class ScrollOfCurse extends Scroll {

	private static final String[] badBuffs = {
			BuffFactory.BLINDNESS,
			BuffFactory.CHARM,
			BuffFactory.ROOTS,
			BuffFactory.SLOW,
			BuffFactory.VERTIGO,
			BuffFactory.WEAKNESS
	};

	@SuppressWarnings("unchecked")
	@Override
	protected void doRead(@NotNull Char reader) {
		CharUtils.dispelInvisibility(reader);

		reader.getSprite().emitter().burst( ShadowParticle.CURSE, 6 );
		Sample.INSTANCE.play( Assets.SND_CURSED );

		String buffKind = Random.oneOf(badBuffs);
		Buff.prolong( reader, buffKind, 10);

		reader.getBelongings().curseEquipped();

		setKnown();
		reader.spend( TIME_TO_READ );
	}


	public static void curse(Char hero, Item... items) {

		boolean procced = false;
		for(Item item:items) {
			if(!item.isCursed()) {
				item.setCursed(true);
				item.setCursedKnown(true);
				if(item.isCursed()) {
					procced = true;
				}
			}
		}

		if (procced) {
			hero.getSprite().emitter().start(ShadowParticle.UP, 0.05f, 10);
		}
	}

	@Override
	public int price() {
		return isKnown() ? 300 * quantity() : super.price();
	}
}
