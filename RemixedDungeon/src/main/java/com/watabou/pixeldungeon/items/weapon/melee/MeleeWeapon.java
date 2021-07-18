/*
 * Pixel Dungeon
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.watabou.pixeldungeon.items.weapon.melee;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.weapon.Weapon;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Random;

public class MeleeWeapon extends Weapon {

	{
		setDefaultAction(AC_EQUIP);
	}
	
	private int tier;
	
	public MeleeWeapon( int tier, float acu, float dly ) {
		this.tier = tier;
		
		ACU = acu;
		DLY = dly;
		
		setSTR(typicalSTR());
		
		MIN = min();
		MAX = max();
	}
	
	protected int min() {
		return tier;
	}
	
	protected int max() {
		return (int)((tier * tier - tier + 10) / ACU * DLY);
	}
	
	@Override
	public Item upgrade() {
		return upgrade( false );
	}
	
	public Item upgrade( boolean enchant ) {
		setSTR(Math.max(2, this.requiredSTR() -1));

		MIN++;
		MAX += tier;
		
		return super.upgrade( enchant );
	}
	
	public Item safeUpgrade() {
		return upgrade( getEnchantment() != null );
	}
	
	@Override
	public Item degrade() {		
		setSTR(this.requiredSTR() + 1);

		MIN = Math.max(MIN-1, 0);
		MAX = Math.max(MAX-tier, 1);

		return super.degrade();
	}
	
	public int typicalSTR() {
		return 8 + tier * 2;
	}
	
	@Override
	public String info() {
		final String p = "\n\n";
		
		StringBuilder info = new StringBuilder( desc() );

        String typical  = StringsManager.getVar(R.string.MeleeWeapon_Info1b);
        String upgraded = StringsManager.getVar(R.string.MeleeWeapon_Info1c);
        String degraded = StringsManager.getVar(R.string.MeleeWeapon_Info1d);
		String quality = isLevelKnown() && level() != 0 ? (level() > 0 ? upgraded : degraded) : typical;
		info.append(p);
        info.append(Utils.capitalize(Utils.format(StringsManager.getVar(R.string.MeleeWeapon_Info1a), name, quality, tier)));
		info.append(" ");
		
		if (isLevelKnown()) {
            info.append(Utils.format(StringsManager.getVar(R.string.MeleeWeapon_Info2a), (MIN + (MAX - MIN) / 2)));
		} else {
            info.append(Utils.format(StringsManager.getVar(R.string.MeleeWeapon_Info2b), (min() + (max() - min()) / 2), typicalSTR()));
			if (typicalSTR() > Dungeon.hero.effectiveSTR()) {
                info.append(" ").append(StringsManager.getVar(R.string.MeleeWeapon_Info2c));
			}
		}

		quality = Utils.EMPTY_STRING;
		info.append(" ");
		if (DLY != 1f) {
            quality += (DLY < 1f ? StringsManager.getVar(R.string.MeleeWeapon_Info3b) : StringsManager.getVar(R.string.MeleeWeapon_Info3c));
			if (ACU != 1f) {
				quality += " ";
				if ((ACU > 1f) == (DLY < 1f)) {
                    quality += StringsManager.getVar(R.string.MeleeWeapon_Info3d);
				} else {
                    quality += StringsManager.getVar(R.string.MeleeWeapon_Info3e);
				}
				quality += " ";
                quality += ACU > 1f ? StringsManager.getVar(R.string.MeleeWeapon_Info3f) : StringsManager.getVar(R.string.MeleeWeapon_Info3g);
			}
            info.append(Utils.format(StringsManager.getVar(R.string.MeleeWeapon_Info3a), quality));
		} else if (ACU != 1f) {
            quality += ACU > 1f ? StringsManager.getVar(R.string.MeleeWeapon_Info3f) : StringsManager.getVar(R.string.MeleeWeapon_Info3g);
            info.append(Utils.format(StringsManager.getVar(R.string.MeleeWeapon_Info3a), quality));
		}

		info.append(" ");
		switch (imbue) {
		case SPEED:
            info.append(StringsManager.getVar(R.string.MeleeWeapon_Info4a));
			break;
		case ACCURACY:
            info.append(StringsManager.getVar(R.string.MeleeWeapon_Info4b));
			break;
		case NONE:
		}

		info.append(" ");
		if (getEnchantment() != null) {
            info.append(StringsManager.getVar(R.string.MeleeWeapon_Info5));
		}

		if (isLevelKnown() && Dungeon.hero.getBelongings().backpack.items.contains( this )) {
			info.append(p);
			if (this.requiredSTR() > Dungeon.hero.effectiveSTR()) {
				info.append(Utils.format(R.string.MeleeWeapon_Info6a, name));
			}
			if (this.requiredSTR() < Dungeon.hero.effectiveSTR()) {
				info.append(Utils.format(R.string.MeleeWeapon_Info6b, name));
			}
		}
		
		if (isEquipped( Dungeon.hero )) {
			info.append(p);
            info.append(Utils.format(R.string.MeleeWeapon_Info7a, name, (isCursed() ? StringsManager.getVar(R.string.MeleeWeapon_Info7b) : Utils.EMPTY_STRING)) );
		} else {
			if (isCursedKnown() && isCursed()) {
				info.append(p);
				info.append(Utils.format(R.string.MeleeWeapon_Info7c, name));
			}
		}
		
		return info.toString();
	}
	
	@Override
	public int price() {
		int price = 20 * (1 << (tier - 1));
		if (getEnchantment() != null) {
			price *= 1.5;
		}
		price = adjustPrice(price);
		return price;
	}

	@Override
	public Item random() {
		super.random();
		
		if (Random.Int( 10 + level() ) == 0) {
			enchant( Enchantment.random() );
		}
		return this;
	}
}
