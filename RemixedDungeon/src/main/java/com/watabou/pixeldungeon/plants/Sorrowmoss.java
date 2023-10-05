
package com.watabou.pixeldungeon.plants;

import com.nyrds.pixeldungeon.levels.objects.Presser;
import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Invisibility;
import com.watabou.pixeldungeon.actors.buffs.Poison;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.particles.PoisonParticle;
import com.watabou.pixeldungeon.items.potions.PotionOfToxicGas;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;

public class Sorrowmoss extends Plant {

	public Sorrowmoss() {
		imageIndex = 2;
	}
	
	public void effect(int pos, Presser ch ) {
		if (ch instanceof Char) {
			Char chr = (Char)ch;
			Buff.affect( chr, Poison.class, Poison.durationFactor( chr ) * (4 + Dungeon.depth / 2) );
		}

		if (Dungeon.isCellVisible(pos)) {
			CellEmitter.center( pos ).burst( PoisonParticle.SPLASH, 3 );
		}
	}

	public static class Seed extends com.watabou.pixeldungeon.plants.Seed {
		{
            plantName = StringsManager.getVar(R.string.Sorrowmoss_Name);

            name = Utils.format(R.string.Plant_Seed, plantName);
			image = 2;

			plantClass = Sorrowmoss.class;
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
				Buff.affect(chr, Poison.class, Poison.durationFactor(chr) * (chr.lvl()) );
				Buff.affect(chr, Invisibility.class, 2 );
			}
		}
	}
}
