
package com.watabou.pixeldungeon.actors.mobs;

import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.ai.Passive;
import com.nyrds.pixeldungeon.items.ItemUtils;
import com.nyrds.pixeldungeon.items.Treasury;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.ItemsList;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.platform.util.TrackedRuntimeException;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Journal;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Bleeding;
import com.watabou.pixeldungeon.actors.buffs.Poison;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.items.EquipableItem;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfPsionicBlast;
import com.watabou.pixeldungeon.items.weapon.Weapon.Enchantment;
import com.watabou.pixeldungeon.items.weapon.enchantments.Death;
import com.watabou.pixeldungeon.items.weapon.enchantments.Leech;
import com.watabou.pixeldungeon.items.weapon.melee.MeleeWeapon;
import com.watabou.pixeldungeon.items.weapon.missiles.MissileWeapon;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.sprites.HeroSpriteDef;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;

import lombok.val;

public class Statue extends Mob {

	public Statue() {
		exp = 0;
		setState(MobAi.getStateByClass(Passive.class));

		hp(ht(15 + Dungeon.depth * 5));
		baseDefenseSkill = 4 + Dungeon.depth;
		baseAttackSkill  = baseDefenseSkill;

		dmgMin = Dungeon.depth / 4 + 1;
		dmgMax = Dungeon.depth;

		baseStr = 18;

		addImmunity( ToxicGas.class );
		addImmunity( Poison.class );
		addResistance( Death.class );
		addResistance( ScrollOfPsionicBlast.class );
		addImmunity( Leech.class );
		addImmunity(Bleeding.class);
	}

	@Override
    public boolean act() {
		if (!isPet() && CharUtils.isVisible(this)) {
			Journal.add( R.string.Journal_Statue );
		}
		return super.act();
	}

	@Override
	public void beckon( int cell ) {
	}

	@Override
	public void destroy() {
		Journal.remove( R.string.Journal_Statue );
		super.destroy();
	}
	
	@Override
	public boolean reset() {
		setState(MobAi.getStateByClass(Passive.class));
		return true;
	}

	@Override
	public String getDescription() {
		val item = getItem();

		if(ItemUtils.usableAsWeapon(item)) {
            return Utils.format(R.string.Statue_Desc, getItem().name());
		}

		if(ItemUtils.usableAsArmor(item)) {
            return Utils.format(R.string.ArmoredStatue_Desc, getItem().name());
		}

		throw new TrackedRuntimeException("Can't equip statue with " + item.getEntityKind());
	}

	@Override
	public CharSprite newSprite() {
		return HeroSpriteDef.createHeroSpriteDef(getItem());
	}

	@NotNull
	public EquipableItem getItem() {
		if(getItemFromSlot(Belongings.Slot.WEAPON) == ItemsList.DUMMY) {
			Item weaponCandidate;
			do {
				weaponCandidate = Treasury.getLevelTreasury().random(Treasury.Category.WEAPON );
			} while (!(weaponCandidate instanceof EquipableItem)
					||!(((EquipableItem) weaponCandidate).goodForMelee())
					|| !ItemUtils.usableAsWeapon((EquipableItem) weaponCandidate)
					|| weaponCandidate.level() < 0
					|| weaponCandidate instanceof MissileWeapon
			);
			EquipableItem chosenItem = ((EquipableItem) weaponCandidate);
			chosenItem.identify();

			if(chosenItem instanceof MeleeWeapon) {
				((MeleeWeapon) chosenItem).enchant(Enchantment.random());
			}

			chosenItem.doEquip(this);
		}
		return getItemFromSlot(Belongings.Slot.WEAPON);
	}
}
