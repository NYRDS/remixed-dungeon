package com.watabou.pixeldungeon.items.armor;

import com.nyrds.pixeldungeon.mechanics.buffs.BuffFactory;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.scenes.CellSelector;
import com.watabou.pixeldungeon.sprites.HeroSpriteDef;
import org.jetbrains.annotations.NotNull;

/**
 * Warrior armor special: leap to the aimed cell, shock-paralysing the
 * origin's surroundings (was WarriorArmor.LeaperCellListener; shared by the
 * warrior data kinds via the ClassArmor "leaper" flag).
 */
public class WarriorLeap implements CellSelector.Listener {

	public static final WarriorLeap INSTANCE = new WarriorLeap();

	private static final int LEAP_TIME = 1;
	private static final int SHOCK_TIME = 3;

	@Override
	public void onSelect(Integer target, @NotNull Char selector) {
		if (target != null) {
			final int pos = selector.getPos();

			if (target != pos) {

				int cell = Ballistica.cast(pos, target, false, true, true);
				if (Actor.findChar(cell) != null && cell != pos) {
					cell = Ballistica.trace[Ballistica.distance - 2];
				}

				CharUtils.dispelInvisibility(selector);

				final int dest = cell;
				((HeroSpriteDef) selector.getSprite()).jump(pos, cell, () -> {
					selector.placeTo(dest);
					selector.level().press(dest, selector);
					selector.observe();

					for (int i = 0; i < Level.NEIGHBOURS8.length; i++) {
						Char mob = Actor.findChar(pos + Level.NEIGHBOURS8[i]);
						if (mob != null && mob != selector) {
							Buff.prolong(mob, BuffFactory.PARALYSIS, SHOCK_TIME);
						}
					}

					CellEmitter.center(dest).burst(Speck.factory(Speck.DUST), 10);
					Camera.main.shake(2, 0.5f);

					selector.spend(LEAP_TIME);
				});
			}
		}
	}

	@Override
	public String prompt() {
		return StringsManager.getVar(R.string.WarriorArmor_Prompt);
	}

	@Override
	public Image icon() {
		return null;
	}

	public boolean mayBeCanceled() {
		return true;
	}
}
