
package com.watabou.pixeldungeon.items.armor;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Invisibility;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.scenes.CellSelector;
import com.watabou.pixeldungeon.sprites.HeroSpriteDef;
import com.watabou.pixeldungeon.utils.GLog;

import org.jetbrains.annotations.NotNull;

public class WarriorArmor extends ClassArmor {
	
	private static final int LEAP_TIME	= 1;
	private static final int SHOCK_TIME	= 3;

	{
		image = 5;
	}
	
	@Override
	public String special() {
		return "WarriorArmor_ACSpecial";
	}
	
	@Override
	public void doSpecial(@NotNull Char user) {
		user.selectCell( leaper );
	}
	
	@Override
	public boolean doEquip(@NotNull Char hero ) {
		if (hero.getHeroClass() == HeroClass.WARRIOR) {
			return super.doEquip( hero );
		} else {
            GLog.w(StringsManager.getVar(R.string.WarriorArmor_NotWarrior));
			return false;
		}
	}
	
	@Override
	public String desc() {
        return StringsManager.getVar(R.string.WarriorArmor_Desc);
    }

	protected static final CellSelector.Listener leaper = new LeaperCellListener();

	private static class LeaperCellListener implements CellSelector.Listener {

		@Override
		public void onSelect(Integer target, @NotNull Char selector) {
			if (target != null) {
				final int pos = selector.getPos();

				if (target != pos) {

					int cell = Ballistica.cast(pos, target, false, true, true);
					if (Actor.findChar(cell) != null && cell != pos) {
						cell = Ballistica.trace[Ballistica.distance - 2];
					}

					Invisibility.dispel(selector);

					final int dest = cell;
					((HeroSpriteDef) selector.getSprite()).jump(pos, cell, () -> {
						selector.placeTo(dest);
						selector.level().press(dest, selector);
						selector.observe();

						for (int i = 0; i < Level.NEIGHBOURS8.length; i++) {
							Char mob = Actor.findChar(pos + Level.NEIGHBOURS8[i]);
							if (mob != null && mob != selector) {
								Buff.prolong(mob, Paralysis.class, SHOCK_TIME);
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
	}
}