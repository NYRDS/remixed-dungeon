
package com.watabou.pixeldungeon.items.rings;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class RingOfThorns extends Ring {
	
	@Override
	public ArtifactBuff buff( ) {
		return new Thorns();
	}
	
	@Override
	public Item random() {
		level(+1);
		return this;
	}
	
	@Override
	public boolean doPickUp(@NotNull Char hero ) {
		identify();
		Badges.validateRingOfThorns();
		Badges.validateItemLevelAcquired( this );
		return super.doPickUp(hero);
	}
	
	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	
	@Override
	public String desc() {
        return isKnown() ? StringsManager.getVar(R.string.RingOfThorns_Info) : super.desc();
	}
	
	public class Thorns extends RingBuff {
		@Override
		public int defenceProc(Char defender, Char enemy, int damage) {
			int dmg = Random.IntRange(0, damage);
			if (dmg > 0) {
				enemy.damage(dmg, this);
			}
			return damage;
		}
	}
}
