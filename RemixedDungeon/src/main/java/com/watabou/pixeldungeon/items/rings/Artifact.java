package com.watabou.pixeldungeon.items.rings;

import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.EquipableItem;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class Artifact extends EquipableItem {

	private static final float TIME_TO_EQUIP = 1f;
	protected ArtifactBuff buff;

	@Override
	public boolean doEquip(Hero hero) {
		setUser(hero);

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
	public boolean isEquipped(Char chr) {
		if(chr instanceof Hero) {
			Hero hero = (Hero)chr;
			return hero.belongings.ring1 == this || hero.belongings.ring2 == this;
		}
		return false;


	}

	public void activate(Char ch) {
		buff = buff();
		if (buff != null) {
			buff.setSource(this);
			buff.attachTo(ch);
		}
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Nullable
	protected ArtifactBuff buff() {
		return null;
	}

	public String getText() {
		return null;
	}

	public int getColor() {
		return 0;
	}
}
