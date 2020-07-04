package com.nyrds.pixeldungeon.mobs.common;

import com.nyrds.pixeldungeon.items.common.GoldenSword;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.CharsList;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.mobs.Statue;
import com.watabou.pixeldungeon.items.EquipableItem;
import com.watabou.pixeldungeon.items.weapon.Weapon;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;

public class GoldenStatue extends Statue {

	public GoldenStatue() {
		hp(ht(15 + Dungeon.depth * 5));
		defenseSkill = 4 + Dungeon.depth;
	}

	@Override
	public String description() {
		return Utils.format(Game.getVar(R.string.GoldenStatue_Desc), getItem().name());
	}

	@NotNull
    @Override
	public EquipableItem getItem() {
		if(getBelongings().weapon == CharsList.DUMMY_ITEM) {
			Weapon weapon = new GoldenSword();
			weapon.identify();
			weapon.upgrade(4);
			weapon.doEquip(this);
		}
		return getBelongings().weapon;
	}
}
