
package com.watabou.pixeldungeon.actors.mobs;

import com.nyrds.pixeldungeon.ai.Hunting;
import com.nyrds.pixeldungeon.mobs.common.IZapper;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Cripple;
import com.watabou.pixeldungeon.actors.buffs.Poison;
import com.watabou.pixeldungeon.items.food.MysteryMeat;
import com.watabou.pixeldungeon.items.potions.PotionOfHealing;
import com.watabou.pixeldungeon.items.weapon.enchantments.Leech;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class Scorpio extends Mob implements IZapper {
	
	public Scorpio() {

		hp(ht(95));
		baseDefenseSkill = 24;
		baseAttackSkill  = 36;
		dmgMin = 20;
		dmgMax = 32;
		dr = 16;
		
		expForKill = 14;
		maxLvl = 25;


		if (Random.Int( 8 ) == 0) {
			collect(new PotionOfHealing());
		} else if (Random.Int( 6 ) == 0) {
			collect(new MysteryMeat());
		}

		addResistance( Leech.class );
		addResistance( Poison.class );
	}

	@Override
	public void onSpawn(Level level) {
		super.onSpawn(level);
		setViewDistance(level.getViewDistance() + 1);
	}

	@Override
    public boolean canAttack(@NotNull Char enemy) {
		return CharUtils.canDoOnlyRangedAttack(this, enemy);
	}

	@Override
	protected int zapProc(@NotNull Char enemy, int damage) {
		if (Random.Int( 2 ) == 0) {
			Buff.prolong( enemy, Cripple.class, Cripple.DURATION );
		}
		return damage;
	}

	@Override
	public boolean getCloser(int target,  boolean ignorePets) {
		if (getState() instanceof Hunting) {
			return enemySeen && getFurther( target );
		} else {
			return super.getCloser( target, ignorePets );
		}
	}
}
