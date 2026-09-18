package com.watabou.pixeldungeon.plants;

import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.mechanics.buffs.BuffFactory;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.items.potions.PotionOfToxicGas;
import com.watabou.pixeldungeon.utils.Utils;
import org.jetbrains.annotations.NotNull;

// batch 20: top-level seed (was Sorrowmoss.Seed). Item kind "Sorrowmoss.Seed"
// unchanged; old saves carrying ...plants.Sorrowmoss$Seed are routed via
// Bundle.addAlias in ItemFactory.
public class SorrowmossSeed extends Seed {
	{
		plantName = StringsManager.getVar(R.string.Sorrowmoss_Name);

		name = Utils.format(R.string.Plant_Seed, plantName);
		image = 2;

		plantKind = "Sorrowmoss";
		alchemyClass = PotionOfToxicGas.class;
	}

	@Override
	public String desc() {
		return StringsManager.getVar(R.string.Sorrowmoss_Desc);
	}

	@Override
	public void _execute(@NotNull Char chr, @NotNull String action ) {

		super._execute(chr, action );

		if (action.equals( CommonActions.AC_EAT )) {
			Buff.affect(chr, BuffFactory.POISON, CharUtils.durationFactor(chr) * (chr.lvl()) );
			Buff.affect(chr, BuffFactory.INVISIBILITY, 2 );
		}
	}
}
