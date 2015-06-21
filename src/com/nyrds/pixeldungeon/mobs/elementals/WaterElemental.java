package com.nyrds.pixeldungeon.mobs.elementals;

import com.nyrds.pixeldungeon.mobs.elementals.sprites.WaterElementalSprite;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Freezing;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.actors.buffs.Frost;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.potions.PotionOfFrost;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfPsionicBlast;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.utils.Random;

public class WaterElemental extends Mob {

	public WaterElemental() {
		spriteClass = WaterElementalSprite.class;
		
		adjustLevel(Dungeon.depth);
		
		loot = new PotionOfFrost();
		lootChance = 0.1f;
	}
	
	private void adjustLevel(int depth) {
		hp(ht(depth * 5 + 1));
		defenseSkill = depth * 2 + 1;
		EXP = depth + 1;
		maxLvl = depth + 2;
		
		IMMUNITIES.add( Frost.class );
		IMMUNITIES.add( ScrollOfPsionicBlast.class );
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
		return EXP / 3;
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
			hp(Math.max(hp() + EXP,ht()));
		}
		
		return super.act();
	}
	
	@Override
	public void add( Buff buff ) {
		if (buff instanceof Frost) {
			if (hp() < ht()) {
				hp(hp() + EXP);
				getSprite().emitter().burst( Speck.factory( Speck.HEALING ), 1 );
			}
		} else {
			if (buff instanceof Burning) {
				damage( Random.NormalIntRange( 1, ht() / 3 ), buff );
			}
			super.add( buff );
		}
	}
}
