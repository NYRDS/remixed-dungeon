package com.watabou.pixeldungeon.items.scrolls;

import com.nyrds.pixeldungeon.levels.PredesignedLevel;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.buffs.Invisibility;
import com.watabou.pixeldungeon.actors.mobs.Bestiary;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.actors.mobs.npcs.MirrorImage;
import com.watabou.pixeldungeon.items.wands.WandOfBlink;

public class ScrollOfSummoning extends Scroll {

	@Override
	protected void doRead() {
		if(Dungeon.level.isBossLevel()){
			return;
		}

		int cell = Dungeon.level.getEmptyCellNextTo(getCurUser().getPos());

		if(Dungeon.level.cellValid(cell)){
			Mob mob = Bestiary.mob( Dungeon.level );
			Mob.makePet(mob, getCurUser());
			Dungeon.level.spawnMob(mob);
			WandOfBlink.appear( mob, cell );
		}

		setKnown();
		
		Sample.INSTANCE.play( Assets.SND_READ );
		Invisibility.dispel(getCurUser());
		
		getCurUser().spendAndNext( TIME_TO_READ );
	}
}
