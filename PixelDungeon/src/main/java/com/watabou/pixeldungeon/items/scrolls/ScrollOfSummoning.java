package com.watabou.pixeldungeon.items.scrolls;

import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.buffs.Invisibility;
import com.watabou.pixeldungeon.actors.mobs.Bestiary;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.SpellSprite;
import com.watabou.pixeldungeon.items.wands.WandOfBlink;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

public class ScrollOfSummoning extends Scroll {

	private static final String TXT_SUMMON = Game.getVar(R.string.ScrollOfSummoning_Info_2);
	private static final String TXT_FAILED_CAST = Game.getVar(R.string.Using_Failed_Because_Magic);
	private static final String TXT_FAILED_PET = Game.getVar(R.string.Mob_Cannot_Be_Pet);
	private static final String TXT_FAILED_CELL = Game.getVar(R.string.No_Valid_Cell);

	@Override
	protected void doRead() {
		if(Dungeon.level.isBossLevel()){
			GLog.w( Utils.format(TXT_FAILED_CAST, this.name()) );
			return;
		}

		int cell = Dungeon.level.getEmptyCellNextTo(getCurUser().getPos());

		if(Dungeon.level.cellValid(cell)){
			Mob mob = Bestiary.mob( Dungeon.level );
			GLog.i( TXT_SUMMON );
			if(mob.canBePet()){
				Mob.makePet(mob, getCurUser());
			} else {
				GLog.w( Utils.format(TXT_FAILED_PET, mob.getName()) );
			}
			Dungeon.level.spawnMob(mob);
			WandOfBlink.appear( mob, cell );
		} else {
			GLog.w( TXT_FAILED_CELL );
		}

		setKnown();

		SpellSprite.show( getCurUser(), SpellSprite.SUMMON );
		Sample.INSTANCE.play( Assets.SND_READ );
		Invisibility.dispel(getCurUser());

		getCurUser().spendAndNext( TIME_TO_READ );
	}
}
