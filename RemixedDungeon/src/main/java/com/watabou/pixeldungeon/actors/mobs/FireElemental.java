
package com.watabou.pixeldungeon.actors.mobs;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Bleeding;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.actors.buffs.Frost;
import com.watabou.pixeldungeon.items.potions.PotionOfLiquidFlame;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfPsionicBlast;
import com.watabou.pixeldungeon.items.wands.WandOfFirebolt;
import com.watabou.pixeldungeon.items.weapon.enchantments.Fire;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class FireElemental extends Mob {

	public FireElemental() {

		hp(ht(65));
		baseDefenseSkill = 20;
		baseAttackSkill  = 25;
		
		exp = 10;
		maxLvl = 20;
		dmgMin = 16;
		dmgMax = 20;
		dr = 5;

		flying = true;
		
		loot(PotionOfLiquidFlame.class, 0.1f);
		
		addImmunity( Fire.class );
		addImmunity( Burning.class );
		addImmunity( WandOfFirebolt.class );
		addImmunity( ScrollOfPsionicBlast.class );
		addImmunity( Bleeding.class );
	}

	@Override
	public int attackProc(@NotNull Char enemy, int damage ) {
		if (Random.Int( 2 ) == 0) {
			Buff.affect( enemy, Burning.class ).reignite( enemy );
		}
		
		return damage;
	}
	
	@Override
	public boolean add(Buff buff ) {
		if (buff instanceof Burning) {
		    heal(Random.NormalIntRange( 1, ht() * 4 ), buff);
			return false;
		}

		if (buff instanceof Frost) {
			damage( Random.NormalIntRange( 1, ht() * 2 / 3 ), buff );
			return false;
		}

		return super.add( buff );
	}
}
