
package com.watabou.pixeldungeon.actors.blobs;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Poison;
import com.watabou.pixeldungeon.actors.buffs.Vertigo;
import com.watabou.pixeldungeon.actors.buffs.Weakness;
import com.watabou.pixeldungeon.effects.BlobEmitter;
import com.watabou.pixeldungeon.effects.Speck;

public class MiasmaGas extends Blob {

	@Override
	protected void evolve() {
		super.evolve();

		Char ch;
		for (int i = 0; i < getLength(); i++) {
			if (cur[i] > 0 && (ch = Actor.findChar(i)) != null) {
				if (!ch.immunities().contains("ToxicGas") && !ch.immunities().contains("GasesImmunity")) {
					// Apply Poison - damage over time
					Buff.prolong(ch, Poison.class, Poison.durationFactor(ch) * 3f);

					// Apply Weakness - reduced damage
					Buff.prolong(ch, Weakness.class, Weakness.duration(ch));

					// Apply Vertigo - disorientation
					Buff.prolong(ch, Vertigo.class, Vertigo.duration(ch));
				}
			}
		}
	}

	@Override
	public void use(BlobEmitter emitter) {
		super.use(emitter);

		emitter.pour(Speck.factory(Speck.MIASMA), 0.6f);
	}

	@Override
	public String tileDesc() {
		return StringsManager.getVar(R.string.MiasmaGas_Info);
	}
}
