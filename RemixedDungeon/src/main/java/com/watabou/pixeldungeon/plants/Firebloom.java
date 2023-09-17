
package com.watabou.pixeldungeon.plants;

import com.nyrds.pixeldungeon.levels.objects.Presser;
import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.Fire;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.actors.buffs.Speed;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.particles.FlameParticle;
import com.watabou.pixeldungeon.items.potions.PotionOfLiquidFlame;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;

public class Firebloom extends Plant {

	public Firebloom() {
		imageIndex = 0;
	}

	public void effect(int pos, Presser ch) {
		GameScene.add(Blob.seed(pos, 2, Fire.class));

		if (Dungeon.isCellVisible(pos)) {
			CellEmitter.get(pos).burst(FlameParticle.FACTORY, 5);
		}
	}

	@Override
	public String desc() {
        return StringsManager.getVar(R.string.Firebloom_Desc);
    }

	public static class Seed extends com.watabou.pixeldungeon.plants.Seed {
		{
            plantName = StringsManager.getVar(R.string.Firebloom_Name);

            name = Utils.format(R.string.Plant_Seed, plantName);

			image = 0;

			plantClass = Firebloom.class;
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
				Buff.affect(chr, Burning.class).reignite(chr);
				Buff.affect(chr, Speed.class, Speed.DURATION);
			}
		}
	}
}
