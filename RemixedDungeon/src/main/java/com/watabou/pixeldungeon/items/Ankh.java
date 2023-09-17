
package com.watabou.pixeldungeon.items;

import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.windows.WndResurrect;

import org.jetbrains.annotations.NotNull;

public class Ankh extends Item {

	{
		stackable = true;
        name = StringsManager.getVar(R.string.Ankh_Name);
		image = ItemSpriteSheet.ANKH;
	}

	public static boolean resurrect(@NotNull Char chr, NamedEntityKind cause) {
		Ankh ankh = chr.getBelongings().getItem(Ankh.class);

		if(ankh == null || !ankh.valid()) {
			return false;
		}

		chr.getBelongings().removeItem(ankh);

		if(! (chr instanceof Hero)) {
			chr.resurrect();
			return true;
		}

		GameScene.show(new WndResurrect(ankh, cause));
		return true;
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	@Override
	public boolean isIdentified() {
		return true;
	}
	
	@Override
	public int price() {
		return 50 * quantity();
	}
}
