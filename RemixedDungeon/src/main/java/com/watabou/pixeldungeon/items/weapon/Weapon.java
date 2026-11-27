
package com.watabou.pixeldungeon.items.weapon;

import com.nyrds.LuaInterface;
import com.nyrds.Packable;
import com.nyrds.pixeldungeon.items.ItemUtils;
import com.nyrds.pixeldungeon.mechanics.LuaScript;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.KindOfWeapon;
import com.watabou.pixeldungeon.items.weapon.melee.KindOfBow;
import com.watabou.pixeldungeon.items.weapon.melee.MeleeWeapon;
import com.watabou.pixeldungeon.items.weapon.missiles.MissileWeapon;
import com.watabou.pixeldungeon.sprites.Glowing;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;
import org.luaj.vm2.LuaValue;

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

	@Getter
    @Packable
	public Enchantment enchantment;
	
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
		
		if (this instanceof MissileWeapon || this instanceof KindOfBow) {
			switch (user.getHeroClass()) {
			case WARRIOR:
				encumbrance += 3;
				break;
			case DOCTOR:
				encumbrance += 5;
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

		if(encumbrance > 0 && Math.random() < 0.05) {
			user.yell(Utils.format(R.string.Accuracy_Decreased, this.name()));
		}


		return
			(encumbrance > 0 ? (float)(ACU / Math.pow( 1.5, encumbrance )) : ACU) *
			(imbue == Imbue.ACCURACY ? 1.5f : 1.0f);
	}
	
	@Override
	public float attackDelayFactor(Char user) {

		int encumbrance = STR - user.effectiveSTR();
		if ((this instanceof MissileWeapon || this instanceof KindOfBow) && user.getHeroClass() == HeroClass.HUNTRESS) {
			encumbrance -= 2;
		}

		if(encumbrance > 0 && Math.random() < 0.05) {
			user.yell(Utils.format(R.string.AttackDelay_Increased, this.name()));
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
		return isLevelKnown() ? Utils.format("%s: \"%d\"", super.toString(), STR ) : super.toString();
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

    @Override
	public void fromJson(JSONObject itemDesc) throws JSONException {
		super.fromJson(itemDesc);

		if(itemDesc.has(ENCHANTMENT)) {
			enchantment = Enchantment.byKind(itemDesc.getString(ENCHANTMENT));
		}
	}

	// --- enchantment kinds are data (scripts/enchants/<Kind>.lua, 2026-11-27):
	// one kind-parameterized framework, proc hook per script, weights from desc
	// chance (vanilla: Fire 10, Poison 10, Death 1, Paralysis 2, Leech 1, Slow 2,
	// Swing 3, Piercing 3, Instability 3, Horror 2, Luck 2)
	@LuaInterface
	public static class Enchantment extends Buff implements Bundlable, NamedEntityKind {

		// kind constants for the java-side kind checks (Char/Mob Death
		// resist/immunity, MissileWeapon boomerang reroll)
		public static final String KIND_DEATH = "Death";
		public static final String KIND_PIERCING = "Piercing";
		public static final String KIND_SWING = "Swing";

		private static final Set<String> allKinds = new LinkedHashSet<>();
		private static final Map<String, Float> pool = new LinkedHashMap<>();
		private static boolean kindsLoaded;

		private final String kind;
		private final transient LuaScript kindScript;
		private final Glowing glow;
		// lazy: <Kind>_Name 3 gender forms (the ctor must not read strings -
		// a field initializer would run before the kind lands)
		private String[] txtName;

		public Enchantment(@NotNull String kind) {
			this.kind = kind;
			kindScript = new LuaScript("scripts/enchants/" + kind, "scripts/lib/emptykind", this);

			Glowing parsed = null;
			if (kindScript.hasMethod("desc")) {
				LuaValue desc = kindScript.run("desc").checktable();
				LuaValue glowDesc = desc.rawget("glowing");
				if (!glowDesc.isnil()) {
					parsed = new Glowing(glowDesc.rawget("color").optint(0xFFFFFF),
							(float) glowDesc.rawget("period").optdouble(1f));
				}
			}
			glow = parsed;
		}

		@Override
		public String getEntityKind() {
			return kind;
		}

		@Override
		public String getEntitySystem() {
			return "glyph";
		}

		private static synchronized void loadKinds() {
			if (kindsLoaded) {
				return;
			}
			kindsLoaded = true;
			for (String file : ModdingMode.listResources("scripts/enchants", (dir, name) -> name.endsWith(".lua"))) {
				String kind = file.replace(".lua", Utils.EMPTY_STRING);
				allKinds.add(kind);
				float chance = (float) readChance("scripts/enchants/" + kind);
				if (chance > 0) {
					pool.put(kind, chance);
				}
			}
		}

		private static double readChance(String path) {
			LuaScript script = new LuaScript(path, null);
			if (!script.hasMethod("desc")) {
				return 0;
			}
			LuaValue desc = script.run("desc").checktable();
			return desc.rawget("chance").optdouble(0);
		}

		public static boolean hasKind(String kind) {
			loadKinds();
			return allKinds.contains(kind);
		}

		@Nullable
		public static Enchantment byKind(String kind) {
			if (!hasKind(kind)) {
				return null;
			}
			return new Enchantment(kind);
		}

		@NotNull
		public static Enchantment random() {
			loadKinds();
			float[] chances = new float[pool.size()];
			int i = 0;
			for (float v : pool.values()) {
				chances[i++] = v;
			}
			int idx = Random.chances(chances);
			if (idx < 0) {
				idx = 0;
			}
			return new Enchantment(new ArrayList<>(pool.keySet()).get(idx));
		}

		public boolean proc(Weapon weapon, Char attacker, Char defender, int damage) {
			return kindScript.runOptional("proc", false, weapon, attacker, defender, damage);
		}

		public String name(String weaponName, int gender) {
			if (txtName == null) {
				txtName = Utils.getClassParams(kind, "Name", new String[]{Utils.EMPTY_STRING, Utils.EMPTY_STRING, Utils.EMPTY_STRING}, true);
			}
			try {
				return Utils.format(txtName[gender], weaponName);
			} catch (Exception e) {
				EventCollector.logException(e);
			}
			return weaponName;
		}

		public Glowing glowing() {
			return glow != null ? glow : Glowing.WHITE;
		}
	}
}
