package com.nyrds.retrodungeon.mobs.elementals;

import android.support.annotation.NonNull;

import com.nyrds.retrodungeon.mobs.common.IDepthAdjustable;
import com.nyrds.retrodungeon.mobs.common.MultiKindMob;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Fire;
import com.watabou.pixeldungeon.actors.blobs.Freezing;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.actors.buffs.Frost;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.potions.PotionOfFrost;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfPsionicBlast;
import com.watabou.pixeldungeon.levels.TerrainFlags;
import com.watabou.utils.Random;

public class WaterElemental extends MultiKindMob implements IDepthAdjustable {

	public WaterElemental() {
		adjustStats(Dungeon.depth);
		
		loot = new PotionOfFrost();
		lootChance = 0.1f;
	}
	
	public void adjustStats(int depth) {
		kind = Math.min(depth/5, 4);
		if ( kind > 5 ) { kind = 5; }

		hp(ht(depth * 5 + 1));
		defenseSkill = depth * 2 + 1;
		exp = depth + 1;
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
		return exp / 3;
	}
	
	@Override
	public int attackProc(@NonNull Char enemy, int damage ) {
		if (Random.Int( 2 ) == 0) {
			Freezing.affect( enemy.getPos(), (Fire)Dungeon.level.blobs.get( Fire.class ) );
		}
		
		return damage;
	}

	@Override
	public float speed() {
		if(TerrainFlags.is(Dungeon.level.map[getPos()], TerrainFlags.LIQUID)) {
			return super.speed() * 2f;
		} else {
			return super.speed() * 0.5f;
		}
	}

	@Override
	public boolean act() {
		if (Dungeon.level.water[getPos()] && hp() < ht()) {
			getSprite().emitter().burst( Speck.factory( Speck.HEALING ), 1 );
			hp(Math.min(hp() + exp,ht()));
		}
		
		return super.act();
	}
	
	@Override
	public void add( Buff buff ) {
		if (buff instanceof Frost) {
			if (hp() < ht()) {
				hp(hp() + exp);
				getSprite().emitter().burst( Speck.factory( Speck.HEALING ), 1 );
			}
		} else {
			if(!Dungeon.isLoading()) {
				if (buff instanceof Burning) {
					damage(Random.NormalIntRange(1, ht() / 3), buff);
				}
			}
			super.add( buff );
		}
	}
}
