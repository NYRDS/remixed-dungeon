package com.watabou.pixeldungeon.actors.mobs;

import java.util.HashSet;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Freezing;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.actors.buffs.Frost;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.potions.PotionOfFrost;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfPsionicBlast;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.sprites.WaterElementalSprite;
import com.watabou.utils.Random;

public class WaterElemental extends Mob {

	public WaterElemental() {
		spriteClass = WaterElementalSprite.class;
		
		adjustLevel(Dungeon.depth);
		
		loot = new PotionOfFrost();
		lootChance = 0.1f;
	}
	
	private void adjustLevel(int depth) {
		hp(ht(depth * 5));
		defenseSkill = depth * 2;
		EXP = depth;
		maxLvl = depth + 2;
		
		
	}
	
	@Override
	public int damageRoll() {
		return Random.NormalIntRange( hp() / 2, ht() / 2 );
	}
	
	@Override
	public int attackSkill( Char target ) {
		return defenseSkill / 2;
	}
	
	@Override
	public int dr() {
		return 5;
	}
	
	@Override
	public int attackProc( Char enemy, int damage ) {
		if (Random.Int( 2 ) == 0) {
			Freezing.affect( enemy.pos, null );
		}
		
		return damage;
	}
	
	@Override
	public boolean act() {
		if (Level.water[pos] && hp() < ht()) {
			getSprite().emitter().burst( Speck.factory( Speck.HEALING ), 1 );
			hp(hp() + EXP);
		}
		
		return super.act();
	}
	
	@Override
	public void add( Buff buff ) {
		if (buff instanceof Frost) {
			if (hp() < ht()) {
				hp(hp() + 1);
				getSprite().emitter().burst( Speck.factory( Speck.HEALING ), 1 );
			}
		} else {
			if (buff instanceof Burning) {
				damage( Random.NormalIntRange( 1, ht() * 2 / 3 ), buff );
			}
			super.add( buff );
		}
	}
	
	private static final HashSet<Class<?>> IMMUNITIES = new HashSet<Class<?>>();
	static {
		IMMUNITIES.add( Frost.class );
		IMMUNITIES.add( ScrollOfPsionicBlast.class );
	}
	
	@Override
	public HashSet<Class<?>> immunities() {
		return IMMUNITIES;
	}
}
