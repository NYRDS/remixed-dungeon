
package com.watabou.pixeldungeon.items;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Random;

public class KindOfWeapon extends EquipableItem {

	public static final float TIME_TO_EQUIP = 1f;

	public static final String SWORD_ATTACK = "sword";
	public static final String SPEAR_ATTACK = "spear";
	public static final String BOW_ATTACK   = "bow";
	public static final String STAFF_ATTACK = "staff";
	public static final String HEAVY_ATTACK = "heavy";
	public static final String WAND_ATTACK  = STAFF_ATTACK;
	public static final String KUSARIGAMA_ATTACK  = "kusarigama";
    public static final String CROSSBOW_ATTACK = "crossbow";

    protected String animation_class = NO_ANIMATION;

	public int		MIN	= 0;
	public int		MAX = 1;

	@Override
	public Belongings.Slot slot(Belongings belongings) {

		if(belongings.slotBlocked(Belongings.Slot.WEAPON)
				&& belongings.slotBlocked(Belongings.Slot.LEFT_HAND)) {
			return Belongings.Slot.WEAPON;
		}

		if(belongings.slotBlocked(Belongings.Slot.WEAPON)) {
			return Belongings.Slot.LEFT_HAND;
		}
		return Belongings.Slot.WEAPON;
	}

	public int damageRoll(Char user) {
		return Random.NormalIntRange( MIN, MAX );
	}

	@Override
	public String getAttackAnimationClass() {
		return animation_class;
	}

	@Override
	public void equippedCursed() {
        super.equippedCursed();
    }

	@Override
	public boolean goodForMelee() {
        return super.goodForMelee();
    }
}
