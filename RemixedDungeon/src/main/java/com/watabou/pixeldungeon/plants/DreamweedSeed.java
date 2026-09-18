package com.watabou.pixeldungeon.plants;

import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.mechanics.buffs.BuffFactory;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.items.potions.PotionOfInvisibility;
import com.watabou.pixeldungeon.utils.Utils;
import org.jetbrains.annotations.NotNull;

// batch 20: top-level seed (was Dreamweed.Seed). Item kind "Dreamweed.Seed"
// unchanged; old saves carrying ...plants.Dreamweed$Seed are routed via
// Bundle.addAlias in ItemFactory.
public class DreamweedSeed extends Seed {
	{
		plantName = StringsManager.getVar(R.string.Dreamweed_Name);

		name = Utils.format(R.string.Plant_Seed, plantName);
		image = 3;

		plantKind = "Dreamweed";
		alchemyClass = PotionOfInvisibility.class;
	}

	@Override
	public String desc() {
		return StringsManager.getVar(R.string.Dreamweed_Desc);
	}

	@Override
	public void _execute(@NotNull Char chr, @NotNull String action ) {

		super._execute(chr, action );

		if (action.equals( CommonActions.AC_EAT )) {
			Buff.affect(chr, BuffFactory.VERTIGO, 10f * 2);
			Buff.affect(chr, BuffFactory.MIND_VISION, 1);
		}
	}
}
