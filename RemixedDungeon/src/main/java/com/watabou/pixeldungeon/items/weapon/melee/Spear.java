
package com.watabou.pixeldungeon.items.weapon.melee;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

public class Spear extends Polearm {

	public Spear() {
		super( 2, 1f, 1.5f );
		image = ItemSpriteSheet.SPEAR;
	}
	
	@Override
	public String desc() {
        return StringsManager.getVar(R.string.Spear_Info);
    }
}
