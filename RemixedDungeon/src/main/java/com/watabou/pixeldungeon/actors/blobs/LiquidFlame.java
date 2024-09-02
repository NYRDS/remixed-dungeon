package com.watabou.pixeldungeon.actors.blobs;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.effects.BlobEmitter;
import com.watabou.pixeldungeon.effects.particles.FlameParticle;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.utils.BArray;

import java.util.Arrays;

public class LiquidFlame extends Blob {

	@Override
	protected void evolve() {

		final Level level = Dungeon.level;
		boolean[] flammable = level.flammable;

		boolean[] notBlocking = BArray.not(BArray.or(level.solid, level.water, null), null);

		Arrays.fill(off,0);

		for (int i = 1; i < getHeight() - 1; i++) {

			int from = i * getWidth() + 1;
			int to = from + getWidth() - 2;

			for (int pos = from; pos < to; pos++) {
				for (int delta : Level.NEIGHBOURS5) {
					int cell = pos + delta;
					if (cur[pos] > 0 && flammable[cell]) {
						cur[cell] += 10;
						Fire.burn(cell);
					}
				}
			}
		}

		for (int i = 1; i < getHeight() - 1; i++) {

			int from = i * getWidth() + 1;
			int to = from + getWidth() - 2;

			for (int pos = from; pos < to; pos++) {
				int value;
				if (notBlocking[pos]) {
					int count = 1;
					int sum = cur[pos];

					for (int delta : Level.NEIGHBOURS4) {
						int cell = pos + delta;
						if (notBlocking[cell]) {
							sum += cur[cell];
							count++;
						}
					}

					value = sum >= count ? (sum / count) - 1 : 0;

					if (value > 0) {
						Fire.burn(pos);
					}
				} else {
					value = cur[pos]/2;
				}
				off[pos] = value;
				setVolume(getVolume() + value);
			}
		}
	}

	@Override
	public void use(BlobEmitter emitter) {
		super.use(emitter);
		emitter.start(FlameParticle.FACTORY, 0.03f, 0);
	}

	@Override
	public String tileDesc() {
        return StringsManager.getVar(R.string.Fire_Info);
    }
}
