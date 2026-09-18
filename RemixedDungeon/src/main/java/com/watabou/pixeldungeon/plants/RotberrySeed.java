package com.watabou.pixeldungeon.plants;

import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.ParalyticGas;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.bags.Bag;
import com.watabou.pixeldungeon.items.potions.PotionOfStrength;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import org.jetbrains.annotations.NotNull;

// batch 20: top-level seed (was Rotberry.Seed). Item kind "Rotberry.Seed"
// unchanged; old saves carrying ...plants.Rotberry$Seed (or the pre-split
// ...npcs.WandMaker$Rotberry$Seed) are routed via Bundle.addAlias in ItemFactory.
public class RotberrySeed extends Seed {
	{
		plantName = StringsManager.getVar(R.string.WandMaker_RotberryName);

		name = Utils.format(R.string.Plant_Seed, plantName);
		image = 7;

		plantKind = "Rotberry";
		alchemyClass = PotionOfStrength.class;
	}

	@Override
	public void _execute(@NotNull Char chr, @NotNull String action) {

		super._execute(chr, action);

		if (action.equals(CommonActions.AC_EAT)) {
			GameScene.add(Blob.seed(chr.getPos(), 100, ToxicGas.class));
			GameScene.add(Blob.seed(chr.getPos(), 100, ParalyticGas.class));
		}
	}

	@Override
	public Item burn(int cell) {
		return this;
	}

	@Override
	public boolean collect(@NotNull Bag container) {
		if (super.collect(container)) {

			CharUtils.challengeAllMobs(getOwner(), Assets.SND_CHALLENGE);
			if (getOwner() == Dungeon.hero) {
				GLog.w(StringsManager.getVar(R.string.WandMaker_RotberryInfo));
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String desc() {
		return StringsManager.getVar(R.string.WandMaker_RotberryDesc);
	}
}
