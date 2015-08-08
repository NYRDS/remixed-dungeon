package com.nyrds.pixeldungeon.mobs.spiders;

import com.nyrds.pixeldungeon.mobs.spiders.sprites.SpiderQueenSprite;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Poison;
import com.watabou.pixeldungeon.actors.buffs.Regeneration;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.Generator;
import com.watabou.pixeldungeon.items.weapon.enchantments.Death;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfWeaponUpgrade;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.utils.Random;

@SuppressWarnings("unused")
public class SpiderQueen extends Mob {
	
	public SpiderQueen() {
		spriteClass = SpiderQueenSprite.class;
		
		hp(ht(120));
		defenseSkill = 18;
		
		EXP = 11;
		maxLvl = 21;
		
		loot = ScrollOfWeaponUpgrade.class;
		lootChance = 1f;
		
		RESISTANCES.add( Death.class );
		
		Buff.affect(this, Regeneration.class);
	}
	
	@Override
	protected boolean act(){
		if(Random.Int(0, 20) == 0 && !SpiderEgg.laid(pos)) {
			SpiderEgg.lay(pos);
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
		if (Dungeon.level.adjacent(pos, enemy.pos) && hp() > ht() / 2 ) {
			return true;
		}
		return false;
	}
	
	@Override
	protected boolean getCloser( int target ) {
		if (hp() < ht() / 2) {
			if (state == HUNTING && Dungeon.level.distance(pos, target) < 5) {
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
		return 20;
	}
	
	@Override
	public int dr() {
		return 8;
	}
}
