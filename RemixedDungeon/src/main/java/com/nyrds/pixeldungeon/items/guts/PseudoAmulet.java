package com.nyrds.pixeldungeon.items.guts;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

public class PseudoAmulet extends Item {

	public PseudoAmulet() {

		image  = ItemSpriteSheet.AMULET;
        name = StringsManager.getVar(R.string.Amulet_Name);
        info = StringsManager.getVar(R.string.Amulet_Info);
	}
	
	@Override
	public Item pick(Char ch, int pos ) {
		return CharUtils.tryToSpawnMimic(this,ch, pos, "MimicAmulet");
	}

}
