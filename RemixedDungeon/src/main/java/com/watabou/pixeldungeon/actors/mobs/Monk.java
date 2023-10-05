
package com.watabou.pixeldungeon.actors.mobs;

import com.nyrds.pixeldungeon.items.Treasury;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Amok;
import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.actors.mobs.npcs.Imp;
import com.watabou.pixeldungeon.items.EquipableItem;
import com.watabou.pixeldungeon.items.weapon.melee.Knuckles;
import com.watabou.pixeldungeon.sprites.MonkSprite;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class Monk extends Mob {

	public Monk() {
		spriteClass = MonkSprite.class;
		
		hp(ht(70));
		baseDefenseSkill = 30;
		baseAttackSkill  = 30;
		dmgMin = 12;
		dmgMax = 16;
		dr = 2;
		
		exp = 11;
		maxLvl = 21;
		
		loot(Treasury.Category.FOOD, 0.153f);

		addImmunity( Amok.class );
		addImmunity( Terror.class );
	}

	
	@Override
	protected float _attackDelay() {
		return 0.5f;
	}

	@Override
	public void die(@NotNull NamedEntityKind cause) {
		Imp.Quest.process( this );
		
		super.die( cause );
	}
	
	@Override
	public int attackProc(@NotNull Char enemy, int damage ) {
		
		if (Random.Int( 6 ) == 0) {
			EquipableItem weapon = enemy.getItemFromSlot(Belongings.Slot.WEAPON);

			if (!(weapon instanceof Knuckles) && !weapon.isCursed() && enemy.getBelongings().drop(weapon)) {
                GLog.w(StringsManager.getVar(R.string.Monk_Disarm), getName(), weapon.name() );
			}
		}
		
		return damage;
	}
}
