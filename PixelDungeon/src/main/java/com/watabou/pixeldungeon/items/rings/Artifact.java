package com.watabou.pixeldungeon.items.rings;

import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.EquipableItem;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

import java.util.ArrayList;

public class Artifact extends EquipableItem {

	static final float TIME_TO_EQUIP = 1f;
	protected Buff buff;

	@Override
	public boolean doEquip(Hero hero) {
		setCurUser(hero);

		if (hero.belongings.ring1 != null && hero.belongings.ring2 != null) {

			GLog.w(Game.getVar(R.string.Artifact_Limit));
			return false;

		} else {

			if (hero.belongings.ring1 == null) {
				hero.belongings.ring1 = this;
			} else {
				hero.belongings.ring2 = this;
			}

			detach(hero.belongings.backpack);

			activate(hero);

			cursedKnown = true;
			if (cursed) {
				equipCursed(hero);
				GLog.n(Utils.format(Game.getVar(R.string.Ring_Info2), this));
			}
			hero.spendAndNext(Artifact.TIME_TO_EQUIP);
			return true;
		}
	}

	@Override
	public boolean doUnequip(Hero hero, boolean collect, boolean single) {
		if (super.doUnequip(hero, collect, single)) {

			if (hero.belongings.ring1 == this) {
				hero.belongings.ring1 = null;
			} else {
				if (hero.belongings.ring2 == this) {
					hero.belongings.ring2 = null;
				} else { //WTF??
					throw new TrackedRuntimeException("trying unequip unequipped artifact");
				}
			}

			hero.remove(buff);
			buff = null;

			return true;
		} else {
			return false;
		}
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(isEquipped(hero) ? AC_UNEQUIP : AC_EQUIP);
		return actions;
	}

	@Override
	public boolean isEquipped(Hero hero) {
		return hero.belongings.ring1 == this || hero.belongings.ring2 == this;
	}

	public void activate(Char ch) {
		buff = buff();
		if (buff != null) {
			buff.attachTo(ch);
		}
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	protected ArtifactBuff buff() {
		return null;
	}

	public class ArtifactBuff extends Buff {
		@Override
		public boolean dontPack() {
			return true;
		}
	}

	public String getText() {
		return null;
	}

	public int getColor() {
		return 0;
	}
}
