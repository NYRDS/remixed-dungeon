
package com.watabou.pixeldungeon.actors.blobs;
import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.mechanics.buffs.BuffFactory;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.effects.BlobEmitter;
import com.watabou.pixeldungeon.effects.Speck;

@LuaInterface
public class MiasmaGas extends Blob {

	@Override
	protected void evolve() {
		super.evolve();

		Char ch;
		for (int i = 0; i < getLength(); i++) {
			if (cur[i] > 0 && (ch = Actor.findChar(i)) != null) {
				if (!ch.immunities().contains("ToxicGas") && !ch.immunities().contains("GasesImmunity")) {
					// Apply Poison - damage over time
					Buff.prolong(ch, BuffFactory.POISON, CharUtils.durationFactor(ch) * 3f);

					// Apply Weakness - reduced damage
					Buff.prolong(ch, BuffFactory.WEAKNESS, CharUtils.durationFactor(ch) * 40f);

					// Apply Vertigo - disorientation
					Buff.prolong(ch, BuffFactory.VERTIGO, CharUtils.durationFactor(ch) * 10f);
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
