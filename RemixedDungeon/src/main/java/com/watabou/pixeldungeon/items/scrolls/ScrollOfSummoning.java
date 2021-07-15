package com.watabou.pixeldungeon.items.scrolls;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Invisibility;
import com.watabou.pixeldungeon.actors.mobs.Bestiary;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.SpellSprite;
import com.watabou.pixeldungeon.items.wands.WandOfBlink;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;

public class ScrollOfSummoning extends Scroll {

	@Override
	protected void doRead(@NotNull Char reader) {
		Level level = Dungeon.level;

		if(level.isBossLevel() || !level.cellValid(level.randomRespawnCell())) {
			GLog.w( Utils.format(R.string.Using_Failed_Because_Magic, this.name()) );
			return;
		}

		int cell = level.getEmptyCellNextTo(reader.getPos());

		if(level.cellValid(cell)){
			Mob mob = Bestiary.mob( level );
            GLog.i(StringsManager.getVar(R.string.ScrollOfSummoning_Info_2));
			if(mob.canBePet()){
				Mob.makePet(mob, reader.getId());
			} else {
				GLog.w( Utils.format(R.string.Mob_Cannot_Be_Pet, mob.getName()));
			}
			WandOfBlink.appear( mob, cell );
		} else {
            GLog.w(StringsManager.getVar(R.string.No_Valid_Cell));
		}

		setKnown();

		SpellSprite.show( reader, SpellSprite.SUMMON );
		Sample.INSTANCE.play( Assets.SND_READ );
		Invisibility.dispel(reader);

		reader.spend( TIME_TO_READ );
	}
}
