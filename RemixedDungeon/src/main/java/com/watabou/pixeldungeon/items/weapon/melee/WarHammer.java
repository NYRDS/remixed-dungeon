
package com.watabou.pixeldungeon.items.weapon.melee;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

public class WarHammer extends MeleeWeapon {
	{
		image = ItemSpriteSheet.WAR_HAMMER;
		animation_class = HEAVY_ATTACK;
	}
	
	public WarHammer() {
		super( 5, 1.2f, 1f );
	}
	
	@Override
	public String desc() {
        return StringsManager.getVar(R.string.WarHammer_Info);
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
