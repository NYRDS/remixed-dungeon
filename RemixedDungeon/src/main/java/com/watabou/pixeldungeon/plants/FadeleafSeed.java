package com.watabou.pixeldungeon.plants;

import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.mechanics.buffs.BuffFactory;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.items.potions.PotionOfMindVision;
import com.watabou.pixeldungeon.utils.Utils;
import org.jetbrains.annotations.NotNull;

// batch 20: top-level seed (was Fadeleaf.Seed). Item kind "Fadeleaf.Seed"
// unchanged; old saves carrying ...plants.Fadeleaf$Seed are routed via
// Bundle.addAlias in ItemFactory.
public class FadeleafSeed extends Seed {
	{
		plantName = StringsManager.getVar(R.string.Fadeleaf_Name);

		name = Utils.format(R.string.Plant_Seed, plantName);
		image = 6;

		plantKind = "Fadeleaf";
		alchemyClass = PotionOfMindVision.class;
	}

	@Override
	public String desc() {
		return StringsManager.getVar(R.string.Fadeleaf_Desc);
	}

	@Override
	public void _execute(@NotNull Char chr, @NotNull String action ) {

		super._execute(chr, action );

		if (action.equals( CommonActions.AC_EAT )) {
			chr.interrupt();
			CharUtils.teleportRandom(chr);
			chr.spend(Actor.TICK);
			Buff.affect(chr, BuffFactory.VERTIGO, 10f * 2);
		}
	}
}
