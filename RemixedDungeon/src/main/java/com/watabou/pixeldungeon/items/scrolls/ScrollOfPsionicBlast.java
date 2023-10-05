
package com.watabou.pixeldungeon.items.scrolls;

import com.nyrds.platform.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Blindness;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Invisibility;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class ScrollOfPsionicBlast extends Scroll {

	@Override
	protected void doRead(@NotNull Char reader) {
		
		GameScene.flash( 0xFFFFFF );
		
		Sample.INSTANCE.play( Assets.SND_BLAST );
		Invisibility.dispel(reader);
		
		for (Mob mob : Dungeon.level.getCopyOfMobsArray()) {
			if (Dungeon.level.fieldOfView[mob.getPos()]) {
				Buff.prolong( mob, Blindness.class, Random.Int( 3, 6 ) );
				mob.damage( Random.IntRange( 1, mob.ht() * 2 / 3 ), this );
			}
		}
		
		Buff.prolong( reader, Blindness.class, Random.Int( 3, 6 ) );
		Dungeon.observe();
		
		setKnown();

		reader.spend( TIME_TO_READ );
	}

	@Override
	public int price() {
		return isKnown() ? 80 * quantity() : super.price();
	}
}
