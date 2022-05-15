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
package com.watabou.pixeldungeon.items.armor;

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.items.ItemUtils;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.Util;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.EquipableItem;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.armor.glyphs.Affection;
import com.watabou.pixeldungeon.items.armor.glyphs.AntiEntropy;
import com.watabou.pixeldungeon.items.armor.glyphs.Bounce;
import com.watabou.pixeldungeon.items.armor.glyphs.Displacement;
import com.watabou.pixeldungeon.items.armor.glyphs.Entanglement;
import com.watabou.pixeldungeon.items.armor.glyphs.Metabolism;
import com.watabou.pixeldungeon.items.armor.glyphs.Multiplicity;
import com.watabou.pixeldungeon.items.armor.glyphs.Potential;
import com.watabou.pixeldungeon.items.armor.glyphs.Stench;
import com.watabou.pixeldungeon.items.armor.glyphs.Viscosity;
import com.watabou.pixeldungeon.sprites.Glowing;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import lombok.SneakyThrows;

public class Armor extends EquipableItem {

	protected boolean hasHelmet = false;
	protected boolean hasCollar = false;
	protected boolean coverHair = false;
	protected boolean coverFacialHair = false;

	public int tier;

	private int hitsToKnow = 10;

	@Packable
	public Glyph glyph;
	
	public Armor( int tier ) {
		imageFile = "items/armor.png";

		this.tier = tier;
	}
	
	private static final String GLYPH	= "glyph";


	@Override
	public Belongings.Slot slot(Belongings belongings) {
		return Belongings.Slot.ARMOR;
	}

	@Override
	public Item upgrade() {
		return upgrade( false );
	}
	
	public Item upgrade( boolean inscribe ) {
		
		if (glyph != null) {
			if (!inscribe && Random.Int( level() ) > 0 && !Dungeon.isLoading()) {
                GLog.w(StringsManager.getVar(R.string.Armor_Incompatible));
				inscribe( null );
			}
		} else {
			if (inscribe) {
				inscribe( Glyph.random() );
			}
		}

		return super.upgrade();
	}

	public int defenceProc(Char attacker, Char defender, int damage ) {
		if (glyph != null) {
			damage = glyph.defenceProc( this, attacker, defender, damage );
		}
		
		if (!isLevelKnown()) {
			if (--hitsToKnow <= 0) {
				setLevelKnown(true);
                GLog.w(StringsManager.getVar(R.string.Armor_Identify), name(), toString() );
				Badges.validateItemLevelAcquired( this );
			}
		}
		
		return damage;
	}
	
	@NotNull
    @Override
	public String toString() {
        return isLevelKnown() ? Utils.format(R.string.Armor_ToString, super.toString(), requiredSTR() ) : super.toString();
	}
	
	@Override
	public String name() {
		return glyph == null ? super.name() : glyph.name( super.name() );
	}
	
	@Override
	public String info() {
		final String p = "\n\n";
		
		StringBuilder info = new StringBuilder( desc() );
		
		String name = name();

		final Char hero = Dungeon.hero;

		if (isLevelKnown()) {
			info.append(p);
            info.append(Utils.capitalize(Utils.format(R.string.Armor_Info1, name, Math.max( effectiveDr(), 0 ))));
			
			if (requiredSTR() > hero.effectiveSTR()) {
				if (isEquipped(hero)) {
                    info.append(StringsManager.getVar(R.string.Armor_Info2));
				} else {
                    info.append(StringsManager.getVar(R.string.Armor_Info3));
				}
			}
		} else {
            info.append(Utils.format(R.string.Armor_Info4, name, typicalDR(), typicalSTR()));
			if (typicalSTR() > hero.effectiveSTR()) {
				info.append(" ");
                info.append(StringsManager.getVar(R.string.Armor_Info5));
			}
		}
		info.append(" ");
		if (glyph != null) {
            info.append(StringsManager.getVar(R.string.Armor_Info6));
		}
		
		if (isEquipped(hero)) {
            info.append(Utils.format(R.string.Armor_Info7a, name,
				(isCursed() ? StringsManager.getVar(R.string.Armor_Info7b) : Utils.EMPTY_STRING) ));
		} else {
			if (isCursedKnown() && isCursed()) {
                info.append(Utils.format(R.string.Armor_Info8, name));
			}
		}
		
		return info.toString();
	}
	
	@Override
	public Item random() {

		ItemUtils.random(this);

		if (Random.Int( 10 ) == 0) {
			inscribe( Glyph.random() );
		}
		
		return this;
	}
	
	public int typicalSTR() {
		return 7 + tier * 2;
	}
	
	public int typicalDR() {
		return tier * 2;
	}

	public int effectiveDr() {
		return tier * 2 + level() * tier + (glyph != null ? tier : 0);
	}

	@Override
	public int price() {
		int price = 10 * (1 << (tier - 1));
		if (glyph != null) {
			price *= 1.5;
		}

		price = adjustPrice(price);

		return price;
	}
	
	public void inscribe(Glyph glyph ) {
		this.glyph = glyph;
	}

	@Override
	public Glowing glowing() {
		return glyph != null ? glyph.glowing() : null;
	}

	public boolean hasHelmet() {
		return hasHelmet;
	}

	public boolean hasCollar() {
		return hasCollar;
	}

	public boolean isCoveringHair() {
		return coverHair;
	}

	public boolean isCoveringFacialHair() {
		return coverFacialHair;
	}

	@Override
	public void fromJson(JSONObject itemDesc) throws JSONException {
		super.fromJson(itemDesc);

		if(itemDesc.has(GLYPH)) {
			glyph = Util.byNameFromList(Glyph.glyphs, itemDesc.getString(GLYPH));
		}
	}

	@Override
	public void equippedCursed() {
        GLog.n(StringsManager.getVar(R.string.Armor_EquipCursed), name() );
	}

	@Override
	public int requiredSTR() {
		return Math.max(typicalSTR() - level(),2);
	}

	public static abstract class Glyph implements Bundlable, NamedEntityKind {
		
		private static final Class<?>[] glyphs = new Class<?>[]{ 
			Bounce.class, Affection.class, AntiEntropy.class, Multiplicity.class, 
			Potential.class, Metabolism.class, Stench.class, Viscosity.class,
			Displacement.class, Entanglement.class };
		
		private static final float[] chances= new float[]{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
			
		public abstract int defenceProc(Armor armor, Char attacker, Char defender, int damage );
		
		public String name() {
            return name(StringsManager.getVar(R.string.Armor_Glyph));
		}
		
		public String name( String armorName ) {
			return armorName;
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
		}

		@Override
		public void storeInBundle( Bundle bundle ) {
		}

		@Override
		public String getEntityKind() {
			return getClass().getSimpleName();
		}

		@Override
		public boolean dontPack() {
			return false;
		}
		
		public Glowing glowing() {
			return Glowing.WHITE;
		}
		
		public void checkOwner( Char owner ) {
			if (!owner.isAlive() && owner instanceof Hero) {
				((Hero)owner).killerGlyph = this;
				Badges.validateDeathFromGlyph();
			}
		}
		
		@SuppressWarnings("unchecked")
		@SneakyThrows
		public static Glyph random() {
			return ((Class<Glyph>)glyphs[ Random.chances( chances ) ]).newInstance();
		}
	}
}
