package com.watabou.pixeldungeon.plants;

import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.mechanics.buffs.BuffFactory;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.items.potions.PotionOfParalyticGas;
import com.watabou.pixeldungeon.utils.Utils;
import org.jetbrains.annotations.NotNull;

// batch 20: top-level seed (was Earthroot.Seed). Item kind "Earthroot.Seed"
// unchanged; old saves carrying ...plants.Earthroot$Seed are routed via
// Bundle.addAlias in ItemFactory.
public class EarthrootSeed extends Seed {
	{
		plantName = StringsManager.getVar(R.string.Earthroot_Name);

		name = Utils.format(R.string.Plant_Seed, plantName);
		image = 5;

		plantKind = "Earthroot";
		alchemyClass = PotionOfParalyticGas.class;
	}

	@Override
	public String desc() {
		return StringsManager.getVar(R.string.Earthroot_Desc);
	}

	@Override
	public void _execute(@NotNull Char chr, @NotNull String action) {

		super._execute(chr, action);

		if (action.equals(CommonActions.AC_EAT)) {
			Buff.affect(chr, BuffFactory.ROOTS, 25);
			Buff barkskin = Buff.affect(chr, BuffFactory.BARKSKIN);
			if (barkskin.level() < chr.effectiveSTR() / 4) barkskin.level(chr.effectiveSTR() / 4);
		}
	}
}
