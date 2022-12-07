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

import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.android.util.Util;
import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
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
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Armor extends EquipableItem {

	private static final String TXT_EQUIP_CURSED    = Game.getVar(R.string.Armor_EquipCursed);
		
	private static final String TXT_IDENTIFY        = Game.getVar(R.string.Armor_Identify);
	
	private static final String TXT_TO_STRING       = Game.getVar(R.string.Armor_ToString);
	
	private static final String TXT_INCOMPATIBLE    = Game.getVar(R.string.Armor_Incompatible);

	protected boolean hasHelmet;
	protected boolean hasCollar;
	protected boolean coverHair;

	public int tier;
	
	public int STR;
	public int DR;

	private int hitsToKnow = 10;
	
	public Glyph glyph;
	
	public Armor( int tier ) {
		imageFile = "items/armor.png";

		this.tier = tier;
		
		STR = typicalSTR();
		DR = typicalDR();
		hasHelmet = false;
		hasCollar = false;
		coverHair = false;
	}
	
	private static final String GLYPH	= "glyph";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( GLYPH, glyph );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		glyph = (Glyph)bundle.get( GLYPH );
	}
	
	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( isEquipped( hero ) ? AC_UNEQUIP : AC_EQUIP );
		return actions;
	}
	
	@Override
	public boolean doEquip( Hero hero ) {
		
		detach( hero.belongings.backpack );
		
		if (hero.belongings.armor == null || hero.belongings.armor.doUnequip( hero, true, false )) {
			
			hero.belongings.armor = this;
			
			cursedKnown = true;
			if (cursed) {
				equipCursed( hero );
				GLog.n( TXT_EQUIP_CURSED, toString() );
			}
			
			hero.updateLook();
			
			hero.spendAndNext( 2 * time2equip( hero ) );
			return true;
			
		} else {
			
			collect( hero.belongings.backpack );
			return false;
			
		}
	}
	
	@Override
	protected float time2equip( Hero hero ) {
		return hero.speed();
	}
	
	@Override
	public boolean doUnequip( Hero hero, boolean collect, boolean single ) {
		if (super.doUnequip( hero, collect, single )) {
			
			hero.belongings.armor = null;
			hero.updateLook();
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public boolean isEquipped( Hero hero ) {
		return hero.belongings.armor == this;
	}
	
	@Override
	public Item upgrade() {
		return upgrade( false );
	}
	
	public Item upgrade( boolean inscribe ) {
		
		if (glyph != null) {
			if (!inscribe && Random.Int( level() ) > 0) {
				GLog.w( TXT_INCOMPATIBLE );
				inscribe( null );
			}
		} else {
			if (inscribe) {
				inscribe( Glyph.random() );
			}
		}
		
		DR += tier;
		STR = Math.max(STR-1,2);
		
		return super.upgrade();
	}

	@Override
	public Item degrade() {
		DR -= tier;
		STR++;
		
		return super.degrade();
	}
	
	public int proc( Char attacker, Char defender, int damage ) {
		
		if (glyph != null) {
			damage = glyph.proc( this, attacker, defender, damage );
		}
		
		if (!levelKnown) {
			if (--hitsToKnow <= 0) {
				levelKnown = true;
				GLog.w( TXT_IDENTIFY, name(), toString() );
				Badges.validateItemLevelAcquired( this );
			}
		}
		
		return damage;
	}
	
	@Override
	public String toString() {
		return levelKnown ? Utils.format( TXT_TO_STRING, super.toString(), STR ) : super.toString();
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
		
		if (levelKnown) {
			info.append(p);
			info.append(Utils.capitalize(Utils.format(Game.getVar(R.string.Armor_Info1), name, Math.max( DR, 0 ))));
			
			if (STR > Dungeon.hero.effectiveSTR()) {
				if (isEquipped( Dungeon.hero )) {
					info.append(Game.getVar(R.string.Armor_Info2));
				} else {
					info.append(Game.getVar(R.string.Armor_Info3));
				}
			}
		} else {
			info.append(Utils.format(Game.getVar(R.string.Armor_Info4), name, typicalDR(), typicalSTR()));
			if (typicalSTR() > Dungeon.hero.effectiveSTR()) {
				info.append(Game.getVar(R.string.Armor_Info5));
			}
		}
		info.append(" ");
		if (glyph != null) {
			info.append(Game.getVar(R.string.Armor_Info6));
		}
		
		if (isEquipped( Dungeon.hero )) {
			info.append(Utils.format(Game.getVar(R.string.Armor_Info7a), name,
				(cursed ? Game.getVar(R.string.Armor_Info7b) : "") )); 
		} else {
			if (cursedKnown && cursed) {
				info.append(Utils.format(Game.getVar(R.string.Armor_Info8), name));
			}
		}
		
		return info.toString();
	}
	
	@Override
	public Item random() {
		if (Random.Float() < 0.4) {
			int n = 1;
			if (Random.Int( 3 ) == 0) {
				n++;
				if (Random.Int( 3 ) == 0) {
					n++;
				}
			}
			if (Random.Int( 2 ) == 0) {
				upgrade( n );
			} else {
				degrade( n );
				cursed = true;
			}
		}
		
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
	
	@Override
	public int price() {
		int price = 10 * (1 << (tier - 1));
		if (glyph != null) {
			price *= 1.5;
		}
		if (cursed && cursedKnown) {
			price /= 2;
		}
		if (levelKnown) {
			if (level() > 0) {
				price *= (level() + 1);
			} else if (level() < 0) {
				price /= (1 - level());
			}
		}
		if (price < 1) {
			price = 1;
		}
		return price;
	}
	
	public Armor inscribe( Glyph glyph ) {
		
		if (glyph != null && this.glyph == null) {
			DR += tier;
		} else if (glyph == null && this.glyph != null) {
			DR -= tier;
		}
		
		this.glyph = glyph;
		
		return this;
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return glyph != null ? glyph.glowing() : null;
	}

	public boolean isHasHelmet() {
		return hasHelmet;
	}

	public boolean isHasCollar() {
		return hasCollar;
	}

	public boolean isCoveringHair() {
		return coverHair;
	}

	@Override
	public void fromJson(JSONObject itemDesc) throws JSONException {
		super.fromJson(itemDesc);

		if(itemDesc.has(GLYPH)) {
			glyph = Util.byNameFromList(Glyph.glyphs, itemDesc.getString(GLYPH));
		}
	}

	public static abstract class Glyph implements Bundlable {
		
		private static final Class<?>[] glyphs = new Class<?>[]{ 
			Bounce.class, Affection.class, AntiEntropy.class, Multiplicity.class, 
			Potential.class, Metabolism.class, Stench.class, Viscosity.class,
			Displacement.class, Entanglement.class };
		
		private static final float[] chances= new float[]{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
			
		public abstract int proc( Armor armor, Char attacker, Char defender, int damage );
		
		public String name() {
			return name( Game.getVar(R.string.Armor_Glyph));
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
		public boolean dontPack() {
			return false;
		}
		
		public ItemSprite.Glowing glowing() {
			return ItemSprite.Glowing.WHITE;
		}
		
		public void checkOwner( Char owner ) {
			if (!owner.isAlive() && owner instanceof Hero) {
				((Hero)owner).killerGlyph = this;
				Badges.validateDeathFromGlyph();
			}
		}
		
		@SuppressWarnings("unchecked")
		public static Glyph random() {
			try {
				return ((Class<Glyph>)glyphs[ Random.chances( chances ) ]).newInstance();
			} catch (Exception e) {
				throw new TrackedRuntimeException(e);
			}
		}
		
	}
}
