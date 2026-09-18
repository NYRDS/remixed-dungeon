package com.watabou.pixeldungeon.plants;

import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.mechanics.buffs.BuffFactory;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.items.potions.PotionOfHealing;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Random;
import org.jetbrains.annotations.NotNull;

// batch 20: top-level seed (was Sungrass.Seed). Item kind "Sungrass.Seed"
// unchanged; old saves carrying ...plants.Sungrass$Seed are routed via
// Bundle.addAlias in ItemFactory.
public class SungrassSeed extends Seed {
	{
		plantName = StringsManager.getVar(R.string.Sungrass_Name);

		name = Utils.format(R.string.Plant_Seed, plantName);
		image = 4;

		plantKind = "Sungrass";
		alchemyClass = PotionOfHealing.class;
	}

	@Override
	public String desc() {
		return StringsManager.getVar(R.string.Sungrass_Desc);
	}

	@Override
	public void _execute(@NotNull Char chr, @NotNull String action) {

		super._execute(chr, action);

		if (action.equals(CommonActions.AC_EAT)) {

			Buff.affect(chr, BuffFactory.CHARM, CharUtils.charmDurationFactor(chr) * Random.IntRange(10, 15));

			chr.heal(Random.Int(0, Math.max((chr.ht() - chr.hp()) / 4, 15)), this);
		}
	}
}
