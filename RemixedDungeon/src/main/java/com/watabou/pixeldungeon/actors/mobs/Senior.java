
package com.watabou.pixeldungeon.actors.mobs;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Stun;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class Senior extends Monk {

	{
		spriteClass = "spritesDesc/Senior.json";
		dmgMin = 12;
		dmgMax = 20;
	}
	
	@Override
	public int attackProc(@NotNull Char enemy, int damage ) {
		if (Random.Int( 10 ) == 0) {
			Buff.prolong( enemy, Stun.class, 1.1f );
		}
		return super.attackProc( enemy, damage );
	}
	
	@Override
	public boolean actMeleeAttack(Char enemy) {
		if (Random.Float() < 0.3f) {
			// Play kick animation with callback to trigger attack logic
			getSprite().playExtra("kick", new Callback() {
				@Override
				public void call() {
					// After kick animation completes, perform the actual attack
					if (isAlive()) {
						onAttackComplete();
					}
				}
			});
			getSprite().turnTo(getPos(), enemy.getPos());
			spend(attackDelay());
			return false;
		} else {
			return super.actMeleeAttack(enemy);
		}
	}
}
