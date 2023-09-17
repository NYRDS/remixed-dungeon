
package com.watabou.pixeldungeon.items.weapon;

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.items.ItemUtils;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.Util;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.KindOfWeapon;
import com.watabou.pixeldungeon.items.weapon.enchantments.Death;
import com.watabou.pixeldungeon.items.weapon.enchantments.Fire;
import com.watabou.pixeldungeon.items.weapon.enchantments.Horror;
import com.watabou.pixeldungeon.items.weapon.enchantments.Instability;
import com.watabou.pixeldungeon.items.weapon.enchantments.Leech;
import com.watabou.pixeldungeon.items.weapon.enchantments.Luck;
import com.watabou.pixeldungeon.items.weapon.enchantments.Paralysis;
import com.watabou.pixeldungeon.items.weapon.enchantments.Piercing;
import com.watabou.pixeldungeon.items.weapon.enchantments.Poison;
import com.watabou.pixeldungeon.items.weapon.enchantments.Slow;
import com.watabou.pixeldungeon.items.weapon.enchantments.Swing;
import com.watabou.pixeldungeon.items.weapon.melee.KindOfBow;
import com.watabou.pixeldungeon.items.weapon.melee.MeleeWeapon;
import com.watabou.pixeldungeon.items.weapon.missiles.MissileWeapon;
import com.watabou.pixeldungeon.sprites.Glowing;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;

@EqualsAndHashCode(callSuper = true)
public class Weapon extends KindOfWeapon {

	private int		STR	= 10;
	public float	ACU	= 1;
	public float	DLY	= 1f;

	protected boolean enchatable = true;

	{
		gender = Utils.genderFromString(getClassParam("Gender", "neuter", true));
	}

	@Override
	public int requiredSTR() {
		return STR;
	}

	public void setSTR(int STR) {
		this.STR = STR;
	}

	public enum Imbue {
		NONE, SPEED, ACCURACY
	}
	public Imbue imbue = Imbue.NONE;
	
	private int hitsToKnow = 20;

	@Packable
	private Enchantment enchantment;
	
	public void usedForHit() {
		if (!isLevelKnown() && --hitsToKnow <= 0) {
			setLevelKnown(true);
            GLog.i(StringsManager.getVar(R.string.Weapon_Identify), name(), toString());
			Badges.validateItemLevelAcquired(this);
		}
	}

	@Override
	public void attackProc(Char attacker, Char defender, int damage ) {
		
		if (getEnchantment() != null) {
			getEnchantment().proc( this, attacker, defender, damage );
		}
		
		usedForHit();
	}
	
	private static final String ENCHANTMENT	= "enchantment";
	private static final String IMBUE		= "imbue";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( IMBUE, imbue );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		imbue = bundle.getEnum( IMBUE, Imbue.class );
	}
	
	@Override
	public float accuracyFactor(Char user) {
		
		int encumbrance = STR - user.effectiveSTR();
		
		if (this instanceof MissileWeapon) {
			switch (user.getHeroClass()) {
			case WARRIOR:
				encumbrance += 3;
				break;
			case HUNTRESS:
				encumbrance -= 2;
				break;
			default:
			}
		}
		
		if (this instanceof MeleeWeapon && !(this instanceof KindOfBow)) {
			if( user.getHeroClass() == HeroClass.ELF) {
				encumbrance += 3;
			}
		}
		
		return 
			(encumbrance > 0 ? (float)(ACU / Math.pow( 1.5, encumbrance )) : ACU) *
			(imbue == Imbue.ACCURACY ? 1.5f : 1.0f);
	}
	
	@Override
	public float attackDelayFactor(Char user) {

		int encumbrance = STR - user.effectiveSTR();
		if (this instanceof MissileWeapon && user.getHeroClass() == HeroClass.HUNTRESS) {
			encumbrance -= 2;
		}
		
		return 
			(encumbrance > 0 ? (float)(DLY * Math.pow( 1.2, encumbrance )) : DLY) *
			(imbue == Imbue.SPEED ? 0.6f : 1.0f);
	}
	
	@Override
	public int damageRoll(Char hero ) {
		
		int damage = super.damageRoll( hero );
		int exStr = hero.effectiveSTR() - STR;

		if(exStr > 0) {
			if (hero.rangedWeapon.valid()  && (hero.getHeroClass() == HeroClass.HUNTRESS)) {
				damage += Random.IntRange(0, exStr);
			}

			if (hero.getHeroClass() == HeroClass.GNOLL) {
				damage += Random.IntRange(0, exStr);
			}
		}
		return damage;
	}
	
	public Item upgrade( boolean enchant ) {		
		if (getEnchantment() != null) {
			if (!enchant && Random.Int( level() ) > 0 && !Dungeon.isLoading()) {
                GLog.w(StringsManager.getVar(R.string.Weapon_Incompatible));
				enchant( null );
			}
		} else {
			if (enchant) {
				enchant( Enchantment.random() );
			}
		}
		
		return super.upgrade();
	}

	@NotNull
    @Override
	public String toString() {
		return isLevelKnown() ? Utils.format("%s: %d", super.toString(), STR ) : super.toString();
	}
	
	@Override
	public String name() {
		return getEnchantment() == null ? super.name() : getEnchantment().name( super.name(), gender );
	}

	@Override
	public Item random() {
		return ItemUtils.random(this);
	}

	public Weapon enchant( Enchantment ench ) {
		if(enchatable) {
			enchantment = ench;
		} else {
			enchantment = null;
		}
		return this;
	}
	
	public boolean isEnchanted() {
		return getEnchantment() != null;
	}
	
	@Override
	public Glowing glowing() {
		return getEnchantment() != null ? getEnchantment().glowing() : null;
	}
	
	public Enchantment getEnchantment() {
		return enchantment;
	}

	@Override
	public void fromJson(JSONObject itemDesc) throws JSONException {
		super.fromJson(itemDesc);

		if(itemDesc.has(ENCHANTMENT)) {
			enchantment = Util.byNameFromList(Enchantment.enchants, itemDesc.getString(ENCHANTMENT));
		}
	}

	public static abstract class Enchantment implements Bundlable, NamedEntityKind {
		
		final String[] TXT_NAME = Utils.getClassParams(getEntityKind(), "Name", new String[]{Utils.EMPTY_STRING, Utils.EMPTY_STRING, Utils.EMPTY_STRING}, true);
		
		private static final Class<?>[] enchants = new Class<?>[]{
			Fire.class, Poison.class, Death.class, Paralysis.class, Leech.class,
			Slow.class, Swing.class, Piercing.class, Instability.class, Horror.class, Luck.class };

		private static final float[] chances= new float[]{ 10, 10, 1, 2, 1, 2, 3, 3, 3, 2, 2 };
			
		public abstract boolean proc( Weapon weapon, Char attacker, Char defender, int damage );
		
		public String name( String weaponName, int gender) {
			try{
				return Utils.format( TXT_NAME[gender], weaponName );
			} catch (Exception e) {
				EventCollector.logException(e);
			}
			return weaponName;
		}

		@Override
		public String name() {
			return getEntityKind();
		}

		public String getEntityKind() {
			return getClass().getSimpleName();
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
		}

		@Override
		public void storeInBundle( Bundle bundle ) {
		}

		public boolean dontPack() {
			return false;
		}
		
		public Glowing glowing() {
			return Glowing.WHITE;
		}
		
		@SuppressWarnings("unchecked")
		@SneakyThrows
		@NotNull
		public static Enchantment random() {
			return ((Class<Enchantment>)enchants[ Random.chances( chances ) ]).newInstance();
		}
	}
}
