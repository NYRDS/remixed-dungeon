
package com.watabou.pixeldungeon.items.weapon.melee;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

public class Knuckles extends MeleeWeapon {
	{
		image = ItemSpriteSheet.KNUCKLEDUSTER;
	}
	
	public Knuckles() {
		super( 1, 1f, 0.5f );
	}
	
	@Override
	public String desc() {
        return StringsManager.getVar(R.string.Knuckles_Info);
    }
}
