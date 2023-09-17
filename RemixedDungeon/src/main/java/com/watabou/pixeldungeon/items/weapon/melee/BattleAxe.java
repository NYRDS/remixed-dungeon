
package com.watabou.pixeldungeon.items.weapon.melee;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

public class BattleAxe extends MeleeWeapon {
	{
		image = ItemSpriteSheet.BATTLE_AXE;
		animation_class = HEAVY_ATTACK;
	}
	
	public BattleAxe() {
		super( 4, 1.2f, 1f );
	}
	
	@Override
	public String desc() {
        return StringsManager.getVar(R.string.BattleAxe_Info);
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
