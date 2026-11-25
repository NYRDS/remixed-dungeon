
package com.watabou.pixeldungeon.items.armor;

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.items.ItemUtils;
import com.nyrds.pixeldungeon.mechanics.LuaScript;
import org.luaj.vm2.LuaValue;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.Util;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.ResultDescriptions;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.actors.hero.Doom;
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
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

public class Armor extends EquipableItem {

	protected boolean hasHelmet = false;
	protected boolean hasCollar = false;
	protected boolean coverHair = false;
	protected boolean coverFacialHair = false;

	public final int tier;

	private int hitsToKnow = 10;

	@Packable
	public Glyph glyph;
	
	public Armor( int tier ) {
		imageFile = "items/armor.png";

		this.tier = tier;
	}

	// --- data-kind surface (was one java class per armor) ---
	// desc -> { tier, image, imageFile, hasHelmet, hasCollar, coverHair,
	//   coverFacialHair, drBonus, visualName, price, upgradable, identified,
	//   noAsh, STR, DR, special, gateClass, gateSub }
	protected String armorKind;
	protected boolean kindWeapon;
	protected LuaScript kindScript;
	protected int drBonusKind;
	protected int kindPrice = -1;
	protected boolean kindUpgradable = true;
	protected boolean kindIdentified;
	protected boolean kindNoAsh;
	protected String gateClass = Utils.EMPTY_STRING;
	protected String gateSub = Utils.EMPTY_STRING;

	public Armor(@NotNull String kind) {
		this(kind, "scripts/lib/emptykind");
	}

	public Armor(@NotNull String kind, String fallbackScript) {
		this(1);
		armorKind = kind;
		kindWeapon = true;
		kindScript = new LuaScript("scripts/items/" + kind, fallbackScript, this);

		name = getClassParam("Name", StringsManager.getVar(R.string.Item_Name), false);
		info = getClassParam("Info", StringsManager.getVar(R.string.Item_Info), false);

		if (!kindScript.hasMethod("desc")) {
			return;
		}
		LuaValue desc = kindScript.run("desc").checktable();

		int imageOverride = desc.rawget("image").optint(-1);
		if (imageOverride >= 0) {
			image = imageOverride;
		}
		LuaValue imageFileOverride = desc.rawget("imageFile");
		if (!imageFileOverride.isnil()) {
			imageFile = imageFileOverride.tojstring();
		}
		hasHelmet        = desc.rawget("hasHelmet").optboolean(false);
		hasCollar        = desc.rawget("hasCollar").optboolean(false);
		coverHair        = desc.rawget("coverHair").optboolean(false);
		coverFacialHair  = desc.rawget("coverFacialHair").optboolean(false);
		drBonusKind      = desc.rawget("drBonus").optint(0);
		kindPrice        = desc.rawget("price").optint(-1);
		kindUpgradable   = desc.rawget("upgradable").optboolean(true);
		kindIdentified   = desc.rawget("identified").optboolean(false);
		kindNoAsh        = desc.rawget("noAsh").optboolean(false);
		gateClass        = desc.rawget("gateClass").optjstring(Utils.EMPTY_STRING);
		gateSub          = desc.rawget("gateSub").optjstring(Utils.EMPTY_STRING);

		if (kindIdentified) {
			identify();
		}
	}

	@Override
	public String getEntityKind() {
		return kindWeapon ? armorKind : super.getEntityKind();
	}

	@Override
	public boolean isUpgradable() {
		if (kindWeapon) {
			return kindUpgradable;
		}
		return super.isUpgradable();
	}

	@Override
	public boolean isIdentified() {
		if (kindWeapon && kindIdentified) {
			return true;
		}
		return super.isIdentified();
	}

	@Override
	public Item burn(int cell) {
		if (kindWeapon && kindNoAsh) {
			// ClothArmor: burns away without leaving an ash pile
			return null;
		}
		return super.burn(cell);
	}


	@Override
	public String getVisualName() {
		if (kindWeapon) {
			return kindScript.runOptional("visualName", super.getVisualName());
		}
		return super.getVisualName();
	}

	// equip/unequip hooks (DoctorArmor gas immunity, NecromancerRobe gate)
	@Override
	public boolean doEquip(@NotNull Char hero ) {
		if (kindWeapon) {
			if (!gateClass.isEmpty() && !gateClass.equals(hero.getHeroClass().name())) {
				GLog.w(StringsManager.getVar(R.string.NecromancerArmor_NotNecromancer));
				return false;
			}
			if (!gateSub.isEmpty() && !gateSub.equals(hero.getSubClass().name())) {
				GLog.w(StringsManager.getVar(R.string.NecromancerArmor_NotNecromancer));
				return false;
			}
		}
		if (super.doEquip(hero)) {
			if (kindWeapon) {
				kindScript.runOptionalNoRet("afterEquip", hero);
			}
			return true;
		}
		return false;
	}

	@Override
	protected boolean doUnequip(Char hero, boolean collect, boolean single ) {
		if (kindWeapon) {
			kindScript.runOptionalNoRet("beforeUnequip", hero);
		}
		return super.doUnequip(hero, collect, single);
	}

	// NecromancerRobe: killed foes grant a skill point (Item.charDied pipeline)
	@Override
	public void charDied(@NotNull com.watabou.pixeldungeon.actors.Char chr, @NotNull com.nyrds.pixeldungeon.mechanics.NamedEntityKind cause) {
		if (kindWeapon && kindScript.hasMethod("charDied")) {
			kindScript.runOptionalNoRet("charDied", chr, cause);
			return;
		}
		super.charDied(chr, cause);
	}

	private static final String LUA_DATA = "luaData";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		if (kindWeapon) {
			bundle.put(LUA_DATA, kindScript.run("saveData").checkjstring());
		}
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		if (!kindWeapon) {
			return;
		}
		String luaData = bundle.optString(LUA_DATA, null);
		if (luaData != null) {
			kindScript.run("loadData", luaData);
		} else {
			kindScript.runOptionalNoRet("loadLegacy", bundle);
		}
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
		if (kindWeapon && kindScript.hasMethod("defenceProc")) {
			damage = kindScript.runOptional("defenceProc", damage, attacker, defender, damage);
		}
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
		int dr = tier * 2;
		if (kindWeapon && drBonusKind != 0) {
			dr += drBonusKind;
		}
		return dr;
	}

	public int effectiveDr() {
		int dr = tier * 2 + level() * tier + (glyph != null ? tier : 0);
		if (kindWeapon && drBonusKind != 0) {
			dr += drBonusKind;
		}
		if (kindWeapon) {
			dr = kindScript.runOptional("effectiveDr", dr);
		}
		return dr;
	}

	@Override
	public int price() {
		if (kindWeapon && kindPrice >= 0) {
			return kindPrice;
		}
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

	public static abstract class Glyph extends Buff implements Bundlable, NamedEntityKind, Doom{
		
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

		public Glowing glowing() {
			return Glowing.WHITE;
		}

		@SuppressWarnings("unchecked")
		@SneakyThrows
		public static Glyph random() {
			return ((Class<Glyph>)glyphs[ Random.chances( chances ) ]).newInstance();
		}

		@Override
		public void onHeroDeath() {
			Badges.validateDeathFromGlyph();
			Dungeon.fail(Utils.format(ResultDescriptions.getDescription(ResultDescriptions.Reason.GLYPH),name(), Dungeon.depth));
			GLog.n(StringsManager.getVars(R.array.Char_Kill)[Utils.MASCULINE], name());
		}
	}
}
