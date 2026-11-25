
package com.watabou.pixeldungeon.items.weapon.melee;

import com.nyrds.LuaInterface;
import com.nyrds.Packable;
import com.nyrds.pixeldungeon.items.ItemUtils;
import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.mechanics.LuaScript;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.watabou.pixeldungeon.items.weapon.Weapon;
import com.watabou.pixeldungeon.effects.KusarigamaChain;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndBag;
import com.watabou.utils.Bundle;
import com.watabou.noosa.Image;
import com.nyrds.platform.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.ui.QuickSlot;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.scenes.CellSelector;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.utils.Random;
import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.LuaValue;

import java.util.ArrayList;

public class MeleeWeapon extends Weapon {

	{
		setDefaultAction(CommonActions.AC_EQUIP);
	}
	
	private final int tier;
	
	public MeleeWeapon( int tier, float acu, float dly ) {
		this.tier = tier;
		tierValue = tier;
		
		ACU = acu;
		DLY = dly;
		
		setSTR(typicalSTR());
		
		MIN = min();
		MAX = max();
	}

	// data kinds (was one java class per weapon): tier/acu/dly/image and the
	// behavior flags come from scripts/items/<Kind>.lua (scripts/lib/weapon.lua)
	@NotNull
	private String meleeKind = Utils.EMPTY_STRING;
	protected LuaScript kindScript;
	protected boolean kindWeapon;

	@LuaInterface
	public static final String AC_REFORGE = "ShortSword_ACReforge";

	private static final String AC_PULL = "Kusarigama_Pull";
	private static final float TIME_TO_REFORGE = 2f;
	private static final float TIME_TO_IMPALE = 1.5f;

	private boolean reforger;
	private boolean upgradeNoop;
	private boolean chainAttack;
	private boolean puller;
	private boolean kindUpgradable;
	private boolean kindTwoHanded;

	public MeleeWeapon(@NotNull String kind) {
		this(kind, "scripts/lib/emptykind");
	}

	public MeleeWeapon(@NotNull String kind, String fallbackScript) {
		this.tier = 1; // kind ctors derive everything from the desc table
		meleeKind = kind;
		kindWeapon = true;
		kindScript = new LuaScript("scripts/items/" + kind, fallbackScript, this);

		name = getClassParam("Name", StringsManager.getVar(R.string.Item_Name), false);
		info = getClassParam("Info", StringsManager.getVar(R.string.Item_Info), false);

		if (!kindScript.hasMethod("desc")) {
			return;
		}
		LuaValue desc = kindScript.run("desc").checktable();

		int tierOverride = desc.rawget("tier").optint(1);
		ACU = (float) desc.rawget("acu").optdouble(1);
		DLY = (float) desc.rawget("dly").optdouble(1);
		enchatable = desc.rawget("enchatable").optboolean(true);
		reforger = desc.rawget("reforger").optboolean(false);
		upgradeNoop = desc.rawget("upgradeNoop").optboolean(false);
		chainAttack = desc.rawget("chainAttack").optboolean(false);
		puller = desc.rawget("puller").optboolean(false);
		kindUpgradable = desc.rawget("upgradable").optboolean(true);
		kindTwoHanded = desc.rawget("twoHanded").optboolean(false);

		// tier is final in the data ctor - re-derive the derived stats here
		tierValue = tierOverride;
		setSTR(desc.rawget("STR").optint(typicalSTR()));
		MIN = desc.rawget("min").optint(min());
		MAX = desc.rawget("max").optint(max());

		int imageOverride = desc.rawget("image").optint(-1);
		if (imageOverride >= 0) {
			image = imageOverride;
		}
		LuaValue imageFileOverride = desc.rawget("imageFile");
		if (!imageFileOverride.isnil()) {
			imageFile = imageFileOverride.tojstring();
		}
	}

	// the effective tier for kind-built weapons (the final field stays at the
	// dummy 1 from the fallback super call)
	private int tierValue = 1;
	
	protected int min() {
		return tier();
	}
	
	protected int max() {
		return (int)((tier() * tier() - tier() + 10) / ACU * DLY);
	}
	
	@Override
	public Item upgrade() {
		return upgrade( false );
	}
	
	public Item upgrade( boolean enchant ) {
		if (upgradeNoop) {
			// TitanSword: level never changes
			return this;
		}
		setSTR(Math.max(2, this.requiredSTR() -1));

		MIN++;
		MAX += tier();
		
		return super.upgrade( enchant );
	}
	
	public Item safeUpgrade() {
		return upgrade( getEnchantment() != null );
	}
	
	@Override
	public Item degrade() {		
		setSTR(this.requiredSTR() + 1);

		MIN = Math.max(MIN-1, 0);
		MAX = Math.max(MAX-tier(), 1);

		return super.degrade();
	}
	
	public int typicalSTR() {
		return kindWeapon ? kindScript.runOptional("typicalSTR", 8 + tier() * 2) : 8 + tier * 2;
	}

	// the kind-aware tier (final field for classic ctors, desc for data kinds)
	protected int tier() {
		return kindWeapon ? tierValue : tier;
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
        info.append(Utils.capitalize(Utils.format(R.string.MeleeWeapon_Info1a, name, quality, tier())));
		info.append(" ");

		final Hero hero = Dungeon.hero;
		if (isLevelKnown()) {
            info.append(Utils.format(R.string.MeleeWeapon_Info2a, (MIN + (MAX - MIN) / 2)));
		} else {
            info.append(Utils.format(R.string.MeleeWeapon_Info2b, (min() + (max() - min()) / 2), typicalSTR()));
			if (typicalSTR() > hero.effectiveSTR()) {
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

		if (isLevelKnown() && hero.getBelongings().backpack.items.contains( this )) {
			info.append(p);
			if (this.requiredSTR() > hero.effectiveSTR()) {
				info.append(Utils.format(R.string.MeleeWeapon_Info6a, name));
			}
			if (this.requiredSTR() < hero.effectiveSTR()) {
				info.append(Utils.format(R.string.MeleeWeapon_Info6b, name));
			}
		}
		
		if (isEquipped(hero)) {
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

	// --- data-kind surface ---

	@Override
	public String getEntityKind() {
		return kindWeapon ? meleeKind : super.getEntityKind();
	}

	@Override
	public ArrayList<String> actions(Char hero ) {
		ArrayList<String> actions = super.actions( hero );
		if (reforger && !isEquipped(hero)) {
			actions.add( AC_REFORGE );
		}
		if (puller && isEquipped(hero)) {
			actions.add( AC_PULL );
		}
		return actions;
	}

	@Override
	public void _execute(@NotNull Char chr, @NotNull String action ) {
		if (reforger && AC_REFORGE.equals(action)) {
			chr.getBelongings().setSelectedItem(this);
			GameScene.selectItem(chr, reforgeSelector, WndBag.Mode.WEAPON, StringsManager.getVar(R.string.ShortSword_Select));
			return;
		}
		if (puller && AC_PULL.equals(action)) {
			chr.selectCell(impaler);
			return;
		}
		super._execute(chr, action);
	}

	// was ShortSword.reforge: consume another melee weapon for a safe upgrade
	private final WndBag.Listener reforgeSelector = (item, selector) -> {
		if (item != null && item instanceof MeleeWeapon) {
			selector.getBelongings().removeItem(this);
			Sample.INSTANCE.play( Assets.SND_EVOKE );
			ScrollOfUpgrade.upgrade( selector );
			ItemUtils.evoke( selector );
			GLog.w( StringsManager.getVar(R.string.ShortSword_Reforged), item.name() );
			((MeleeWeapon) item).safeUpgrade();
			selector.spend( TIME_TO_REFORGE );
			Badges.validateItemLevelAcquired( item );
		}
		selector.updateSprite();
		QuickSlot.refresh(selector);
	};

	@Override
	public void preAttack(Char tgt) {
		if (chainAttack && tgt != null) {
			// Kusarigama: draw the chain when striking beyond melee reach
			Char user = getOwner();
			if (user.level().distance(user.getPos(), tgt.getPos()) > 1) {
				drawChain(tgt.getPos(), user);
			}
		}
		super.preAttack(tgt);
	}

	// was Kusarigama.drawChain
	private void drawChain(int tgt, Char caster) {
		CharSprite casterSprite = caster.getSprite();

		casterSprite.zap(tgt);
		casterSprite
				.getParent()
				.add(new KusarigamaChain(casterSprite.center(),
						DungeonTilemap.tileCenterToWorld(tgt)));
	}

	@Override
	public void postAttack(Char tgt) {
		if (kindWeapon && kindScript.hasMethod("postAttack")) {
			kindScript.runOptionalNoRet("postAttack", tgt);
			return;
		}
		super.postAttack(tgt);
	}

	@Override
	public void attackProc(Char attacker, Char defender, int damage) {
		if (kindWeapon && kindScript.hasMethod("attackProc")) {
			// kind-owned proc (GoldenSword semantics: no enchant path, the
			// unenchantable kinds never had one to skip)
			damage = kindScript.runOptional("attackProc", damage, attacker, defender, damage);
			usedForHit();
			return;
		}
		super.attackProc(attacker, defender, damage);
	}

	@Override
	public int range() {
		if (kindWeapon) {
			return kindScript.runOptional("range", super.range());
		}
		return super.range();
	}

	@Override
	public String getAttackAnimationClass() {
		if (kindWeapon) {
			String anim = kindScript.runOptional("anim", Utils.EMPTY_STRING);
			if (!anim.isEmpty()) {
				return anim;
			}
			return super.getAttackAnimationClass();
		}
		return super.getAttackAnimationClass();
	}

	@Override
	public String getVisualName() {
		if (kindWeapon) {
			return kindScript.runOptional("visualName", super.getVisualName());
		}
		return super.getVisualName();
	}

	// chaos-weapon charge flow + per-kind callbacks
	@Override
	public void ownerTakesDamage(int damage) {
		if (kindWeapon) {
			kindScript.runOptionalNoRet("ownerTakesDamage", damage);
			return;
		}
		super.ownerTakesDamage(damage);
	}

	@Override
	public void ownerDoesDamage(int damage) {
		if (kindWeapon) {
			kindScript.runOptionalNoRet("ownerDoesDamage", damage);
			return;
		}
		super.ownerDoesDamage(damage);
	}

	@Override
	public boolean isUpgradable() {
		if (kindWeapon) {
			return kindUpgradable;
		}
		return super.isUpgradable();
	}

	@Override
	public Belongings.Slot blockSlot() {
		if (kindWeapon && kindTwoHanded) {
			return Belongings.Slot.LEFT_HAND;
		}
		return super.blockSlot();
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
			// kind migrated from a java class: read legacy fields straight
			// from the bundle (ChaosSword/Blade charge, chaos image reselect)
			kindScript.runOptionalNoRet("loadLegacy", bundle);
		}
	}



	// was Kusarigama.KusarigamaCellListener: yank the aimed char one cell
	// toward the wielder, chain-drawing regardless of hit
	private final CellSelector.Listener impaler = new CellSelector.Listener() {
		@Override
		public void onSelect(Integer target, @NotNull Char selector) {

			if (target != null) {
				selector.spend(TIME_TO_IMPALE);
				int hitCell = Ballistica.cast(selector.getPos(), target, false, true);

				if (hitCell == selector.getPos()) {
					return;
				}

				if (selector.level().distance(selector.getPos(), hitCell) < 4) {
					Char chr = Actor.findChar(hitCell);

					if (chr != null && chr.isMovable()) {
						chr.placeTo(Ballistica.trace[1]);
						chr.getSprite().move(chr.getPos(), Ballistica.trace[1]);

						selector.observe();
					}

					drawChain(hitCell, selector);
				} else {
					drawChain(Ballistica.trace[4], selector);
				}
			}
		}

		@Override
		public String prompt() {
			return StringsManager.getVar(R.string.Item_DirThrow);
		}

		@Override
		public Image icon() {
			return null;
		}

		public boolean mayBeCanceled() {
			return true;
		}
	};

}