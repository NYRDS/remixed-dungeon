package com.watabou.pixeldungeon.plants;
import com.nyrds.pixeldungeon.levels.objects.Presser;
import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.mechanics.buffs.BuffFactory;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.ParalyticGas;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.bags.Bag;
import com.watabou.pixeldungeon.items.potions.PotionOfStrength;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import org.jetbrains.annotations.NotNull;

// split out of WandMaker.java (batch 16c-2): same inner-Seed layout as the
// other plants. Pre-split saves may carry the old inner-class FQN
// ...npcs.WandMaker$Rotberry$Seed - aliased in ItemFactory (Bundle.addAlias).
public class Rotberry extends Plant {

	{
		imageIndex = 7;
	}

	@Override
	public String name() {
		return StringsManager.getVar(R.string.WandMaker_RotberryName);
	}

	@Override
	public void effect(int pos, Presser ch) {
		GameScene.add(Blob.seed(pos, 100, ToxicGas.class));

		level().animatedDrop(new Seed(), pos);

		if (ch instanceof Char) {
			Buff.prolong((Char) ch, BuffFactory.ROOTS, TICK * 3);
		}
	}

	@Override
	public String desc() {
		return StringsManager.getVar(R.string.WandMaker_RotberryDesc);
	}

	public static class Seed extends com.watabou.pixeldungeon.plants.Seed {
		{
			plantName = StringsManager.getVar(R.string.WandMaker_RotberryName);

			name = Utils.format(R.string.Plant_Seed, plantName);
			image = 7;

			plantClass = Rotberry.class;
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
}
