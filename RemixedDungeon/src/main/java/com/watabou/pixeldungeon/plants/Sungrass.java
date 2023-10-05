
package com.watabou.pixeldungeon.plants;

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.levels.objects.Presser;
import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Charm;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.particles.ShaftParticle;
import com.watabou.pixeldungeon.items.potions.PotionOfHealing;
import com.watabou.pixeldungeon.ui.BuffIndicator;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class Sungrass extends Plant {

	public Sungrass() {
		imageIndex = 4;
	}

	public void effect(int pos, Presser ch) {
		if (ch instanceof Char) {
			Buff.affect((Char)(ch), Health.class);
		}

		if (Dungeon.isCellVisible(pos)) {
			CellEmitter.get(pos).start(ShaftParticle.FACTORY, 0.2f, 3);
		}
	}

	public static class Seed extends com.watabou.pixeldungeon.plants.Seed {
		{
            plantName = StringsManager.getVar(R.string.Sungrass_Name);

            name = Utils.format(R.string.Plant_Seed, plantName);
			image = 4;

			plantClass = Sungrass.class;
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

				Buff.affect(chr, Charm.class, Charm.durationFactor(chr) * Random.IntRange(10, 15));

				chr.heal(Random.Int(0, Math.max((chr.ht() - chr.hp()) / 4, 15)), this);
			}
		}
	}

	public static class Health extends Buff {

		private static final float STEP = 5f;

		@Packable
		private int pos;

		@Override
		public boolean attachTo(@NotNull Char target) {
			pos = target.getPos();
			return super.attachTo(target);
		}

		@Override
		public boolean act() {
			if (target.getPos() != pos || target.hp() >= target.ht()) {
				detach();
			} else {
				target.heal(Math.max( target.ht() / 10, 1),this);

				if (Dungeon.isCellVisible(pos)) {
					CellEmitter.get(pos).start(ShaftParticle.FACTORY, 0.2f, 3);
				}
			}
			spend(STEP);
			return true;
		}

		@Override
		public int icon() {
			return BuffIndicator.HEALING;
		}

		@Override
		public String name() {
            return StringsManager.getVar(R.string.SungrassBuff_Name);
        }

		@Override
		public String desc() {
            return StringsManager.getVar(R.string.SungrassBuff_Info);
        }
	}
}
