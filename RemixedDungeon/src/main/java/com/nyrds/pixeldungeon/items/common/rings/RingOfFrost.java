
package com.nyrds.pixeldungeon.items.common.rings;
import com.nyrds.pixeldungeon.mechanics.buffs.BuffFactory;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.buffs.Buff;
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
					Buff.affect(enemy, BuffFactory.SLOW, CharUtils.durationFactor(enemy) * 10f / 5 + level());
					if (Random.Int(100) < 10 + level()) {
						Buff.affect(enemy, BuffFactory.FROST, CharUtils.durationFactor(enemy) * 5f / 5 + level());
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
