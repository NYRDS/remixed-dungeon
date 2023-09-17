
package com.watabou.pixeldungeon.plants;

import com.nyrds.pixeldungeon.levels.objects.Presser;
import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.ConfusionGas;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.MindVision;
import com.watabou.pixeldungeon.actors.buffs.Vertigo;
import com.watabou.pixeldungeon.items.potions.PotionOfInvisibility;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;

public class Dreamweed extends Plant {

	public Dreamweed() {
		imageIndex = 3;
	}
	
	public void effect(int pos, Presser ch) {
		GameScene.add( Blob.seed( pos, 300 + 20 * Dungeon.depth, ConfusionGas.class ) );
	}

	public static class Seed extends com.watabou.pixeldungeon.plants.Seed {
		{
            plantName = StringsManager.getVar(R.string.Dreamweed_Name);

            name = Utils.format(R.string.Plant_Seed, plantName);
			image = 3;
			
			plantClass = Dreamweed.class;
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
				Buff.affect(chr, Vertigo.class, Vertigo.DURATION * 2);
				Buff.affect(chr, MindVision.class, 1);
			}
		}
	}
}
