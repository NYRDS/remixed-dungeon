
package com.watabou.pixeldungeon.actors.mobs;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.potions.PotionOfHealing;
import com.watabou.pixeldungeon.items.weapon.enchantments.Leech;

import org.jetbrains.annotations.NotNull;

public class Bat extends Mob {

	public Bat() {
		hp(ht(30));
		baseDefenseSkill = 15;
		baseAttackSkill  = 16;
		baseSpeed = 2f;

		dmgMin = 6;
		dmgMax = 12;

		exp = 7;
		maxLvl = 15;
		
		flying = true;
		
		loot(PotionOfHealing.class, 0.125f);

		addResistance( Leech.class );
	}

	@Override
	public int dr() {
		return 4;
	}
	
	@Override
	public int attackProc(@NotNull Char enemy, int damage ) {
		
		heal(damage, enemy);

		return damage;
	}
}
