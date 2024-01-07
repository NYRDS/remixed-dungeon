package com.nyrds.pixeldungeon.mobs.spiders;

import com.nyrds.pixeldungeon.ai.Hunting;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.items.chaos.ChaosCrystal;
import com.nyrds.pixeldungeon.items.common.armor.SpiderArmor;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.actors.buffs.Poison;
import com.watabou.pixeldungeon.actors.mobs.Boss;
import com.watabou.pixeldungeon.items.SpiderCharm;
import com.watabou.pixeldungeon.items.keys.SkeletonKey;
import com.watabou.pixeldungeon.items.weapon.enchantments.Fire;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class SpiderQueen extends Boss {
	public SpiderQueen() {
		hp(ht(120));
		baseDefenseSkill = 18;
		baseAttackSkill  = 21;
		expForKill = 11;

		dmgMin = 12;
		dmgMax = 20;
		dr = 10;


		float dice = Random.Float();
		if( dice < 0.33 ) {
			collect(new ChaosCrystal());
		} else if( dice < 0.66 ){
			collect(new SpiderCharm());
		} else{
			collect(new SpiderArmor());
		}

		collect(new SkeletonKey());

		addImmunity( Fire.class );
		addImmunity( Burning.class );
	}

	@Override
    public boolean act(){
		if(Random.Int(0, 20) == 0) {
			CharUtils.spawnOnNextCell(this, "SpiderEgg", (int) (100 * GameLoop.getDifficultyFactor()));
		}
		
		return super.act();
	}
	
	@Override
	public int attackProc(@NotNull Char enemy, int damage ) {
		if (Random.Int( 2 ) == 0) {
			Buff.affect( enemy, Poison.class, Random.Int( 7, 9 ) * Poison.durationFactor( enemy ) );
		}
		
		return damage;
	}
	
	@Override
    public boolean canAttack(@NotNull Char enemy) {
		return adjacent(enemy) && hp() > ht() / 2;
	}
	
	@Override
	public boolean getCloser(int target) {
		if (hp() < ht() / 2) {
			if (getState() instanceof Hunting && level().distance(getPos(), target) < 5) {
				return getFurther(target);
			}
		}
		return super.getCloser(target);
	}

	@Override
	public void die(@NotNull NamedEntityKind cause) {
		Badges.validateBossSlain(Badges.Badge.SPIDER_QUEEN_SLAIN);
		super.die(cause);
	}
}
