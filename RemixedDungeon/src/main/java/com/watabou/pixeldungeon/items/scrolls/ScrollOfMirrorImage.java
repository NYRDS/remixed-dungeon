
package com.watabou.pixeldungeon.items.scrolls;

import com.nyrds.platform.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Invisibility;
import com.watabou.pixeldungeon.items.wands.WandOfBlink;
import com.watabou.pixeldungeon.levels.Level;

import org.jetbrains.annotations.NotNull;

public class ScrollOfMirrorImage extends Scroll {

	private static final int NIMAGES	= 3;

	@Override
	protected void doRead(@NotNull Char reader) {

		Level level = Dungeon.level;

		int nImages = NIMAGES;
		while (nImages > 0 ) {
			int cell = level.getEmptyCellNextTo(reader.getPos());

			if(!level.cellValid(cell))
				break;

			Char image  = reader.makeClone();
			WandOfBlink.appear( image, cell );
			nImages--;
		}
		
		if (nImages < NIMAGES) {
			setKnown();
		}
		
		Sample.INSTANCE.play( Assets.SND_READ );
		Invisibility.dispel(reader);
		
		reader.spend( TIME_TO_READ );
	}
}
