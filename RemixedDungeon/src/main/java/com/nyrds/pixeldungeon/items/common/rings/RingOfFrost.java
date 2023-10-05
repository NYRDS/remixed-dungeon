
package com.nyrds.pixeldungeon.items.common.rings;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Frost;
import com.watabou.pixeldungeon.actors.buffs.Slow;
import com.watabou.pixeldungeon.items.bags.Bag;
import com.watabou.pixeldungeon.items.rings.Artifact;
import com.watabou.pixeldungeon.items.rings.ArtifactBuff;
import com.watabou.pixeldungeon.sprites.Glowing;
import com.watabou.pixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Random;

public class RingOfFrost extends Artifact {

	public RingOfFrost() {
		imageFile = "items/rings.png";
		image = 13;
		identify();
	}

	@Override
	public Glowing glowing() {
		return new Glowing( 0x00FFFF );
	}

	@Override
	public ArtifactBuff buff( ) {
		return new FrostAura();
	}

	@Override
	public boolean isUpgradable() {
		return true;
	}

	public static class FrostAura extends ArtifactBuff {
		@Override
		public int icon() {
			return BuffIndicator.FROSTAURA;
		}

		@Override
		public int defenceProc(Char defender, Char enemy, int damage) {
			if (enemy.distance(defender) < 2) {
				if (enemy.isAlive()) {
					Buff.affect(enemy, Slow.class, Slow.duration(enemy) / 5 + level());
					if (Random.Int(100) < 10 + level()) {
						Buff.affect(enemy, Frost.class, Frost.duration(enemy) / 5 + level());
					}
					enemy.damage(level() / 2, this);
				}
			}
			return damage;
		}
	}

	@Override
	public String bag() {
		return Bag.KEYRING;
	}
}
