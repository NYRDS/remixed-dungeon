package com.watabou.pixeldungeon.plants;

import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.mechanics.buffs.BuffFactory;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.items.potions.PotionOfLiquidFlame;
import com.watabou.pixeldungeon.utils.Utils;
import org.jetbrains.annotations.NotNull;

// batch 20: top-level seed (was Firebloom.Seed). Item kind "Firebloom.Seed"
// unchanged; old saves carrying ...plants.Firebloom$Seed are routed via
// Bundle.addAlias in ItemFactory.
public class FirebloomSeed extends Seed {
	{
		plantName = StringsManager.getVar(R.string.Firebloom_Name);

		name = Utils.format(R.string.Plant_Seed, plantName);

		image = 0;

		plantKind = "Firebloom";
		alchemyClass = PotionOfLiquidFlame.class;
	}

	@Override
	public String desc() {
		return StringsManager.getVar(R.string.Firebloom_Desc);
	}

	@Override
	public void _execute(@NotNull Char chr, @NotNull String action) {

		super._execute(chr, action);

		if (action.equals(CommonActions.AC_EAT)) {
			CharUtils.ignite(chr);
			Buff.affect(chr, BuffFactory.SPEED, 10f);
		}
	}
}
