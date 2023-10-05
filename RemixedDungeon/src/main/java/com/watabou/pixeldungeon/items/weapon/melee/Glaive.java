
package com.watabou.pixeldungeon.items.weapon.melee;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

public class Glaive extends Polearm {

	public Glaive() {
		super( 5, 1.2f, 1.4f );
		image = ItemSpriteSheet.GLAIVE;
	}

	@Override
	public String desc() {
        return StringsManager.getVar(R.string.Glaive_Info);
    }
}
