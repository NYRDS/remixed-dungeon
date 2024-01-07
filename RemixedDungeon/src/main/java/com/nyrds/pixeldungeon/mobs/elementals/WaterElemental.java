package com.nyrds.pixeldungeon.mobs.elementals;

import com.nyrds.pixeldungeon.mobs.common.IDepthAdjustable;
import com.nyrds.pixeldungeon.mobs.common.MultiKindMob;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Freezing;
import com.watabou.pixeldungeon.actors.buffs.Bleeding;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.actors.buffs.Frost;
import com.watabou.pixeldungeon.items.potions.PotionOfFrost;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfPsionicBlast;
import com.watabou.pixeldungeon.levels.TerrainFlags;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class WaterElemental extends MultiKindMob implements IDepthAdjustable {

	public WaterElemental() {
		adjustStats(Dungeon.depth);
		
		loot(new PotionOfFrost(), 0.1f);
	}
	
	public void adjustStats(int depth) {
		kind = Math.min(depth/5, 4);
		if ( kind > 5 ) { kind = 5; }

		hp(ht(depth * 5 + 1));
		baseDefenseSkill = depth * 2 + 1;
		expForKill = depth + 1;
		maxLvl = depth + 2;
		dr = expForKill /3;
		baseAttackSkill = baseDefenseSkill / 2 + 1;
		dmgMin = ht()/2;
		dmgMax = ht()/2;


		addImmunity( Frost.class );
		addImmunity( ScrollOfPsionicBlast.class );
		addImmunity( Bleeding.class );
	}

	@Override
	public int attackProc(@NotNull Char enemy, int damage ) {
		if (Random.Int( 2 ) == 0) {
			Freezing.affect( enemy.getPos() );
		}
		
		return damage;
	}

	@Override
	public float speed() {
		if(TerrainFlags.is(level().map[getPos()], TerrainFlags.LIQUID)) {
			return super.speed() * 2f;
		}

		return super.speed() * 0.5f;
	}

	@Override
	public boolean act() {
		if (level().water[getPos()]) {
			heal(expForKill,this);
		}
		
		return super.act();
	}
	
	@Override
	public boolean add(Buff buff ) {
		if (buff instanceof Frost) {
			if (hp() < ht()) {
				heal(expForKill, buff);
			}
			return false;
		} else {
			if(!Dungeon.isLoading()) {
				if (buff instanceof Burning) {
					damage(Random.NormalIntRange(1, ht() / 3), buff);
				}
			}
			return super.add( buff );
		}
	}
}
