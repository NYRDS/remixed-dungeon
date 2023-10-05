
package com.watabou.pixeldungeon.items.scrolls;

import com.nyrds.platform.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Blindness;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Charm;
import com.watabou.pixeldungeon.actors.buffs.FlavourBuff;
import com.watabou.pixeldungeon.actors.buffs.Invisibility;
import com.watabou.pixeldungeon.actors.buffs.Roots;
import com.watabou.pixeldungeon.actors.buffs.Slow;
import com.watabou.pixeldungeon.actors.buffs.Vertigo;
import com.watabou.pixeldungeon.actors.buffs.Weakness;
import com.watabou.pixeldungeon.effects.particles.ShadowParticle;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class ScrollOfCurse extends Scroll {

	private static Class<?>[] badBuffs = {
			Blindness.class,
			Charm.class,
			Roots.class,
			Slow.class,
			Vertigo.class,
			Weakness.class
	};

	@SuppressWarnings("unchecked")
	@Override
	protected void doRead(@NotNull Char reader) {
		Invisibility.dispel(reader);

		reader.getSprite().emitter().burst( ShadowParticle.CURSE, 6 );
		Sample.INSTANCE.play( Assets.SND_CURSED );

		Class <? extends FlavourBuff> buffClass = (Class<? extends FlavourBuff>) Random.oneOf(badBuffs);
		Buff.prolong( reader, buffClass, 10);

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
