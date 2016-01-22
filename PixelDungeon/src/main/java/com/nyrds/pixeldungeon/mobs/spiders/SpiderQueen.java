package com.nyrds.pixeldungeon.mobs.spiders;

import com.nyrds.pixeldungeon.items.chaos.ChaosCrystal;
import com.nyrds.pixeldungeon.mobs.spiders.sprites.SpiderQueenSprite;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Poison;
import com.watabou.pixeldungeon.actors.buffs.Regeneration;
import com.watabou.pixeldungeon.actors.mobs.Boss;
import com.watabou.utils.Random;

public class SpiderQueen extends Boss {
	
	public SpiderQueen() {
		spriteClass = SpiderQueenSprite.class;
		
		hp(ht(120));
		defenseSkill = 18;
		
		EXP = 11;
		maxLvl = 21;
		
		loot = new ChaosCrystal();
		lootChance = 1f;
		
		Buff.affect(this, Regeneration.class);
	}
	
	@Override
	protected boolean act(){
		if(Random.Int(0, 20) == 0 && !SpiderEgg.laid(getPos())) {
			SpiderEgg.lay(getPos());
		}
		
		return super.act();
	}
	
	@Override
	public int attackProc( Char enemy, int damage ) {
		if (Random.Int( 2 ) == 0) {
			Buff.affect( enemy, Poison.class ).set( Random.Int( 7, 9 ) * Poison.durationFactor( enemy ) );
		}
		
		return damage;
	}
	
	@Override
	protected boolean canAttack(Char enemy) {
		if (Dungeon.level.adjacent(getPos(), enemy.getPos()) && hp() > ht() / 2 ) {
			return true;
		}
		return false;
	}
	
	@Override
	protected boolean getCloser( int target ) {
		if (hp() < ht() / 2) {
			if (state == HUNTING && Dungeon.level.distance(getPos(), target) < 5) {
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
}
