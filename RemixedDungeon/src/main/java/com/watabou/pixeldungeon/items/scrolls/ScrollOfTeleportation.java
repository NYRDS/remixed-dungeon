
package com.watabou.pixeldungeon.items.scrolls;

import com.nyrds.platform.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.buffs.Invisibility;

import org.jetbrains.annotations.NotNull;

public class ScrollOfTeleportation extends Scroll {

	@Override
	protected void doRead(@NotNull Char reader) {

		Sample.INSTANCE.play( Assets.SND_READ );
		Invisibility.dispel(reader);
		
		CharUtils.teleportRandom( reader);
		setKnown();

		reader.spend( TIME_TO_READ );
	}

	@Override
	public int price() {
		return isKnown() ? 40 * quantity() : super.price();
	}
}
