
package com.watabou.pixeldungeon.items.weapon.melee;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

public class Quarterstaff extends MeleeWeapon {
	{
		image = ItemSpriteSheet.QUARTERSTAFF;
		animation_class = STAFF_ATTACK;
	}
	
	public Quarterstaff() {
		super( 2, 1f, 0.8f );
	}
	
	@Override
	public String desc() {
        return StringsManager.getVar(R.string.Quarterstaff_Info);
    }

	@Override
	public Belongings.Slot slot(Belongings belongings) {
		return Belongings.Slot.WEAPON;
	}

	@Override
	public Belongings.Slot blockSlot() {
		return Belongings.Slot.LEFT_HAND;
	}

}
