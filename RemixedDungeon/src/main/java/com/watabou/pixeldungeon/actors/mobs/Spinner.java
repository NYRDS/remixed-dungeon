
package com.watabou.pixeldungeon.actors.mobs;

import com.nyrds.pixeldungeon.ai.Fleeing;
import com.nyrds.pixeldungeon.ai.Hunting;
import com.nyrds.pixeldungeon.ai.MobAi;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.Web;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Poison;
import com.watabou.pixeldungeon.actors.buffs.Roots;
import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.items.food.MysteryMeat;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class Spinner extends Mob {
	
	public Spinner() {
		hp(ht(50));
		baseDefenseSkill = 14;
		baseAttackSkill  = 20;
		dmgMin = 12;
		dmgMax = 16;
		dr = 6;
		
		exp = 9;
		maxLvl = 16;
		
		loot(new MysteryMeat(), 0.125f);

		addResistance( Poison.class );
		addImmunity( Roots.class );
	}

	@Override
    public boolean act() {
		boolean result = super.act();
		
		if ((getState() instanceof Fleeing) && !hasBuff(Terror.class) && enemySeen && !getEnemy().hasBuff(Poison.class)) {
			setState(MobAi.getStateByClass(Hunting.class));
		}
		return result;
	}
	
	@Override
	public int attackProc(@NotNull Char enemy, int damage ) {
		if (Random.Int( 2 ) == 0) {
			Buff.affect( enemy, Poison.class,Random.Int( 7, 9 ) * Poison.durationFactor( enemy ) );
			setState(MobAi.getStateByClass(Fleeing.class));
		}
		
		return damage;
	}
	
	@Override
	public void move( int step ) {
		if (getState() instanceof Fleeing) {
			GameScene.add( Blob.seed( getPos(), Random.Int( 5, 7 ), Web.class ) );
		}
		super.move( step );
	}

}
