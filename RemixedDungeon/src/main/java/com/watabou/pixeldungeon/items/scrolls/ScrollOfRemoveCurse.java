
package com.watabou.pixeldungeon.items.scrolls;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Invisibility;
import com.watabou.pixeldungeon.actors.buffs.Weakness;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.effects.Flare;
import com.watabou.pixeldungeon.effects.particles.ShadowParticle;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.utils.GLog;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class ScrollOfRemoveCurse extends Scroll {

	@Override
	protected void doRead(@NotNull Char reader) {

		new Flare(6, 32).show(reader.getSprite(), 2f);
		Sample.INSTANCE.play(Assets.SND_READ);
		Invisibility.dispel(reader);

		boolean procced = uncurse(reader.getBelongings());

		Weakness.detach(reader, Weakness.class);

		if (procced) {
            GLog.p(StringsManager.getVar(R.string.ScrollOfRemoveCurse_Proced));
			reader.getSprite().emitter().start(ShadowParticle.UP, 0.05f, 10);
		} else {
            GLog.i(StringsManager.getVar(R.string.ScrollOfRemoveCurse_NoProced));
		}

		setKnown();

		reader.spend(TIME_TO_READ);
	}

	public static void uncurse(Char hero, Item ... items) {

		boolean procced = false;
		for(Item item:items) {
			procced = uncurseItem(procced, item);
		}

		if (procced) {
			hero.getSprite().emitter().start(ShadowParticle.UP, 0.05f, 10);
		}
	}

	private static boolean uncurseItem(boolean procced, Item item) {
		if (item != null && item.isCursed()) {
			item.setCursed(false);
			procced = true;
		}
		return procced;
	}

	public static boolean uncurse(Belongings belongings) {

		Iterator<Item> itemIterator = belongings.iterator();

		boolean procced = false;
		while (itemIterator.hasNext()) {
			procced = uncurseItem(procced, itemIterator.next());
		}

		return procced;
	}

	@Override
	public int price() {
		return isKnown() ? 30 * quantity() : super.price();
	}
}
