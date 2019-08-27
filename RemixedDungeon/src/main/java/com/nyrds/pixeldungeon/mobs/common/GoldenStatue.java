package com.nyrds.pixeldungeon.mobs.common;

import com.nyrds.pixeldungeon.items.common.GoldenSword;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.mobs.Statue;
import com.watabou.pixeldungeon.items.weapon.Weapon;
import com.watabou.pixeldungeon.utils.Utils;

public class GoldenStatue extends Statue {

	public GoldenStatue() {
		hp(ht(15 + Dungeon.depth * 5));
		defenseSkill = 4 + Dungeon.depth;
	}

	@Override
	public String description() {
		return Utils.format(Game.getVar(R.string.GoldenStatue_Desc), getWeapon().name());
	}

	@Override
	public Weapon getWeapon() {
		if(weapon==null) {
			Weapon weapon = new GoldenSword();
			weapon.identify();
			weapon.upgrade(4);
		}
		return weapon;
	}
}
