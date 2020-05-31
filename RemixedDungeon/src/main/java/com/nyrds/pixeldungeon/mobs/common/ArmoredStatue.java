package com.nyrds.pixeldungeon.mobs.common;

import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.ai.Passive;
import com.nyrds.pixeldungeon.items.Treasury;
import com.nyrds.pixeldungeon.utils.CharsList;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Statue;
import com.watabou.pixeldungeon.items.EquipableItem;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.armor.Armor;
import com.watabou.utils.Random;

import lombok.val;

public class ArmoredStatue extends Statue {

	public ArmoredStatue() {
		defenseSkill = 4 + Dungeon.depth*2;
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 4, 8 );
	}

	@Override
	public int attackSkill( Char target ) {
		return (9 + Dungeon.depth) * 2;
	}

	@Override
	public void beckon( int cell ) {
	}

	@Override
	public boolean reset() {
		setState(MobAi.getStateByClass(Passive.class));
		return true;
	}


	@Override
	public EquipableItem getItem() {
		if(getBelongings().armor == CharsList.DUMMY_ITEM) {
			Item armorCandidate;
			do {
				armorCandidate = Treasury.getLevelTreasury().random(Treasury.Category.ARMOR);
			} while (!(armorCandidate instanceof Armor) || armorCandidate.level() < 0);

			val armor = (Armor) armorCandidate;
			armor.identify();
			armor.inscribe(Armor.Glyph.random());

			armor.doEquip(this);
		}
		return getBelongings().armor;
	}
}
