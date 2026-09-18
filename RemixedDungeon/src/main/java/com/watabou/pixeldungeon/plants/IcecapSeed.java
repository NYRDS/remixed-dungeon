package com.watabou.pixeldungeon.plants;

import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.mechanics.buffs.BuffFactory;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.items.potions.PotionOfFrost;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Random;
import org.jetbrains.annotations.NotNull;

// batch 20: top-level seed (was Icecap.Seed). Item kind "Icecap.Seed"
// unchanged; old saves carrying ...plants.Icecap$Seed are routed via
// Bundle.addAlias in ItemFactory.
public class IcecapSeed extends Seed {
	{
		plantName = StringsManager.getVar(R.string.Icecap_Name);

		name = Utils.format(R.string.Plant_Seed, plantName);
		image = 1;

		plantKind = "Icecap";
		alchemyClass = PotionOfFrost.class;
	}

	@Override
	public String desc() {
		return StringsManager.getVar(R.string.Icecap_Desc);
	}

	@Override
	public void _execute(@NotNull Char chr, @NotNull String action) {

		super._execute(chr, action);

		if (action.equals(CommonActions.AC_EAT)) {

			Buff.prolong(chr, BuffFactory.FROST, CharUtils.durationFactor(chr) * 5f * 2);
			chr.heal( Random.Int(0, Math.max((chr.ht() - chr.hp()) / 4, 10) ), this);
		}
	}
}
