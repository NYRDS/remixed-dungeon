package com.watabou.pixeldungeon.items.rings;

import com.nyrds.android.util.TrackedRuntimeException;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.EquipableItem;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class Artifact extends EquipableItem {

	public static final float TIME_TO_EQUIP = 1f;

	@Nullable
	protected ArtifactBuff buff;

	@Override
	public boolean doEquip(Hero hero) {
		setUser(hero);

		return hero.belongings.equip(this, Belongings.Slot.ARTIFACT);
	}

	@Override
	public boolean doUnequip(Char hero, boolean collect, boolean single) {
		if (super.doUnequip(hero, collect, single)) {

			if (hero.getBelongings().ring1 == this) {
				hero.getBelongings().ring1 = null;
			} else {
				if (hero.getBelongings().ring2 == this) {
					hero.getBelongings().ring2 = null;
				} else { //WTF??
					throw new TrackedRuntimeException("trying unequip unequipped artifact");
				}
			}

			if(buff!=null) {
				hero.remove(buff);
				buff = null;
			}

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
