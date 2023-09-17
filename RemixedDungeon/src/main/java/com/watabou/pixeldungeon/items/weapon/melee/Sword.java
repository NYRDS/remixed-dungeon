
package com.watabou.pixeldungeon.items.weapon.melee;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

public class Sword extends MeleeWeapon {
	{
		image = ItemSpriteSheet.SWORD;
		animation_class = SWORD_ATTACK;
	}
	
	public Sword() {
		super( 3, 1f, 1f );
	}
	
	@Override
	public String desc() {
        return StringsManager.getVar(R.string.Sword_Info);
    }
}
