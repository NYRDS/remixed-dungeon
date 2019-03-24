package com.nyrds.pixeldungeon.mobs.spiders;

import com.nyrds.pixeldungeon.ai.Hunting;
import com.nyrds.pixeldungeon.items.chaos.ChaosCrystal;
import com.nyrds.pixeldungeon.items.common.armor.SpiderArmor;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Poison;
import com.watabou.pixeldungeon.actors.mobs.Boss;
import com.watabou.pixeldungeon.items.SpiderCharm;
import com.watabou.pixeldungeon.items.keys.SkeletonKey;
import com.watabou.utils.Random;

import androidx.annotation.NonNull;

public class SpiderQueen extends Boss {
	
	public SpiderQueen() {
		hp(ht(120));
		defenseSkill = 18;
		
		exp = 11;

		float dice = Random.Float();
		if( dice < 0.33 ) {
			loot = new ChaosCrystal();
		} else if( dice < 0.66 ){
			loot = new SpiderCharm();
		} else{
			loot = new SpiderArmor();
		}
		lootChance = 1f;
	}
	
	@Override
    public boolean act(){
		if(Random.Int(0, 20) == 0 && !SpiderEgg.laid(getPos())) {
			SpiderEgg.lay(getPos());
		}
		
		return super.act();
	}
	
	@Override
	public int attackProc(@NonNull Char enemy, int damage ) {
		if (Random.Int( 2 ) == 0) {
			Buff.affect( enemy, Poison.class ).set( Random.Int( 7, 9 ) * Poison.durationFactor( enemy ) );
		}
		
		return damage;
	}
	
	@Override
    public boolean canAttack(Char enemy) {
		return Dungeon.level.adjacent(getPos(), enemy.getPos()) && hp() > ht() / 2;
	}
	
	@Override
	public boolean getCloser(int target) {
		if (hp() < ht() / 2) {
			if (getState() instanceof Hunting && level().distance(getPos(), target) < 5) {
				return getFurther(target);
			}
			return super.getCloser(target);
		} else {
			return super.getCloser( target );
		}
	}
	
	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 12, 20 );
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 22;
	}
	
	@Override
	public int dr() {
		return 10;
	}

	@Override
	public void die(NamedEntityKind cause) {
		super.die(cause);
		Dungeon.level.drop( new SkeletonKey(), getPos() ).sprite.drop();
		Badges.validateBossSlain(Badges.Badge.SPIDER_QUEEN_SLAIN);
	}
}
