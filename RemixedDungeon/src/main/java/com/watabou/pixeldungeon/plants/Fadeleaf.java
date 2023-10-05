
package com.watabou.pixeldungeon.plants;

import com.nyrds.pixeldungeon.levels.objects.Presser;
import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Vertigo;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.potions.PotionOfMindVision;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;

public class Fadeleaf extends Plant {

	public Fadeleaf () {
		imageIndex = 6;
	}
	
	public void effect(int pos, Presser ch) {

		if(ch instanceof Char) {
			CharUtils.teleportRandom((Char) ch);
		}

		if (Dungeon.isCellVisible(pos)) {
			CellEmitter.get( pos ).start( Speck.factory( Speck.LIGHT ), 0.2f, 3 );
		}		
	}

	@Override
	public String desc() {
        return StringsManager.getVar(R.string.Fadeleaf_Desc);
    }
	
	public static class Seed extends com.watabou.pixeldungeon.plants.Seed {
		{
            plantName = StringsManager.getVar(R.string.Fadeleaf_Name);

            name = Utils.format(R.string.Plant_Seed, plantName);
			image = 6;
			
			plantClass = Fadeleaf.class;
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
				Buff.affect(chr, Vertigo.class, Vertigo.DURATION * 2);
			}
		}
	}
}
