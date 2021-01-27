package com.nyrds.pixeldungeon.mobs.common;

import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.ai.Passive;
import com.nyrds.pixeldungeon.items.ItemUtils;
import com.nyrds.pixeldungeon.items.Treasury;
import com.nyrds.pixeldungeon.utils.ItemsList;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.actors.mobs.Statue;
import com.watabou.pixeldungeon.items.EquipableItem;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.armor.Armor;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

import lombok.val;

public class ArmoredStatue extends Statue {

	public ArmoredStatue() {
		baseDefenseSkill = 4 + Dungeon.depth*2;
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


	@NotNull
    @Override
	public EquipableItem getItem() {
		if(getItemFromSlot(Belongings.Slot.ARMOR) == ItemsList.DUMMY) {
			Item armorCandidate;
			do {
				armorCandidate = Treasury.getLevelTreasury().random(Treasury.Category.ARMOR);
			} while (!(armorCandidate instanceof EquipableItem)
						|| armorCandidate.level() < 0
						|| !(ItemUtils.usableAsArmor((EquipableItem) armorCandidate))
					);


			val armor = (EquipableItem)armorCandidate;
			armor.identify();

			if(armor instanceof Armor) {
				((Armor)armor).inscribe(Armor.Glyph.random());
			}

			armor.doEquip(this);
		}
		return getItemFromSlot(Belongings.Slot.ARMOR);
	}
}
