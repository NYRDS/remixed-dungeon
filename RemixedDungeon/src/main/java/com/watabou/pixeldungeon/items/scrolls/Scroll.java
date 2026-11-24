package com.watabou.pixeldungeon.items.scrolls;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.items.common.UnknownItem;
import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.mechanics.LuaScript;
import com.nyrds.pixeldungeon.mechanics.buffs.BuffFactory;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.actors.mobs.Bestiary;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.effects.SpellSprite;
import com.watabou.pixeldungeon.effects.particles.EnergyParticle;
import com.watabou.pixeldungeon.effects.particles.ShadowParticle;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.ItemStatusHandler;
import com.watabou.pixeldungeon.items.ItemStatusHandlerKinds;
import com.watabou.pixeldungeon.items.bags.ScrollHolder;
import com.watabou.pixeldungeon.items.wands.WandOfBlink;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.levels.TerrainFlags;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.LuaValue;

/**
 * Data scrolls: one java kind-parameterized framework, per-kind read effects
 * in scripts/items/<Kind>.lua built with scripts/lib/scroll.lua (was 12
 * per-scroll java classes). Kind == the legacy class simple name; the
 * identification handler keeps the legacy "class &lt;pkg&gt;.&lt;Kind&gt;"
 * bundle keys verbatim, so per-class-era saves stay loadable. The
 * selection-driven scrolls (Identify, Upgrade, WeaponUpgrade) and the
 * BlankScroll keep their thin java classes: their UI coupling is
 * framework-shaped, their onItemSelected bodies are three-liners.
 *
 * Lua hooks (self = the script table, item = the scroll):
 *   desc(self)                       -> { price }  (known price, default 15)
 *   doRead(self, item, reader)       -> the read effect
 *   onItemSelected(self, item, selector) -> inventory-scroll target applied
 */
public class Scroll extends Item implements UnknownItem {
	protected static final float TIME_TO_READ	= 1f;

	public static final String IDENTIFY        = "ScrollOfIdentify";
	public static final String MAGIC_MAPPING   = "ScrollOfMagicMapping";
	public static final String RECHARGING      = "ScrollOfRecharging";
	public static final String REMOVE_CURSE    = "ScrollOfRemoveCurse";
	public static final String TELEPORTATION   = "ScrollOfTeleportation";
	public static final String UPGRADE         = "ScrollOfUpgrade";
	public static final String CHALLENGE       = "ScrollOfChallenge";
	public static final String TERROR          = "ScrollOfTerror";
	public static final String LULLABY         = "ScrollOfLullaby";
	public static final String WEAPON_UPGRADE  = "ScrollOfWeaponUpgrade";
	public static final String PSIONIC_BLAST   = "ScrollOfPsionicBlast";
	public static final String MIRROR_IMAGE    = "ScrollOfMirrorImage";
	public static final String DOMINATION      = "ScrollOfDomination";
	public static final String SUMMONING       = "ScrollOfSummoning";
	public static final String CURSE           = "ScrollOfCurse";
	public static final String BLANK           = "BlankScroll";

	// legacy class package: identification bundle keys keep this exact form
	private static final String LEGACY_PACKAGE = "com.watabou.pixeldungeon.items.scrolls";

	private static final String[] scrolls = {
		IDENTIFY,
		MAGIC_MAPPING,
		RECHARGING,
		REMOVE_CURSE,
		TELEPORTATION,
		UPGRADE,
		CHALLENGE,
		TERROR,
		LULLABY,
		WEAPON_UPGRADE,
		PSIONIC_BLAST,
		MIRROR_IMAGE,
		DOMINATION,
		SUMMONING,
		CURSE
	};

	private static final HashSet<String> inscribableScrolls = new HashSet<>(java.util.Arrays.asList(
		IDENTIFY,
		MAGIC_MAPPING,
		RECHARGING,
		REMOVE_CURSE,
		TELEPORTATION,
		UPGRADE,
		CHALLENGE,
		TERROR,
		LULLABY,
		PSIONIC_BLAST,
		MIRROR_IMAGE,
		DOMINATION,
		SUMMONING,
		CURSE
	));

	private static final Integer[] images = {
		ItemSpriteSheet.SCROLL_KAUNAN,
		ItemSpriteSheet.SCROLL_SOWILO,
		ItemSpriteSheet.SCROLL_LAGUZ,
		ItemSpriteSheet.SCROLL_YNGVI,
		ItemSpriteSheet.SCROLL_GYFU,
		ItemSpriteSheet.SCROLL_RAIDO,
		ItemSpriteSheet.SCROLL_ISAZ,
		ItemSpriteSheet.SCROLL_MANNAZ,
		ItemSpriteSheet.SCROLL_NAUDIZ,
		ItemSpriteSheet.SCROLL_BERKANAN,
		ItemSpriteSheet.SCROLL_ODAL,
		ItemSpriteSheet.SCROLL_TIWAZ,
		ItemSpriteSheet.SCROLL_ANSUZ,
		ItemSpriteSheet.SCROLL_IWAZ,
		ItemSpriteSheet.SCROLL_ALGIZ,
		ItemSpriteSheet.SCROLL_DAGAZ};

	private static ItemStatusHandlerKinds handler;
	private String rune;

	public static void initLabels() {
		handler = new ItemStatusHandlerKinds(LEGACY_PACKAGE, scrolls, images);
	}

	public static void save( Bundle bundle ) {
		handler.save( bundle );
	}

	public static void restore( Bundle bundle ) {
		handler = new ItemStatusHandlerKinds(LEGACY_PACKAGE, scrolls, images, bundle);
	}

	// restore fallback for the bare legacy tag
	public Scroll() {
		this(IDENTIFY);
	}

	public Scroll(@NotNull String kind) {
		stackable     = true;
		setDefaultAction(CommonActions.AC_READ);

		potionKind = kind;
		kindScript = ModdingMode.isResourceExists("scripts/items/" + kind + ".lua")
				? new LuaScript("scripts/items/" + kind, this)
				: null;

		// Item's name/info initialize before the kind is known - re-resolve
		name = getClassParam("Name", StringsManager.getVar(R.string.Item_Name), false);
		info = getClassParam("Info", StringsManager.getVar(R.string.Item_Info), false);
		applyKindDesc();

		if (kind.equals(BLANK)) {
			return;
		}
		if (handler != null) {
			image = handler.index( potionKind );
		}
		rune  = StringsManager.getVars(R.array.Scroll_Runes)[ItemStatusHandler.indexByImage(image,images)];
	}

	@NotNull
	protected final String potionKind;
	// null for the java-kept kinds without a scripts/items/<Kind>.lua
	@Nullable
	protected final LuaScript kindScript;

	private float kindPrice = 15;

	private void applyKindDesc() {
		if (kindScript == null || !kindScript.hasMethod("desc")) {
			return;
		}
		LuaValue desc = kindScript.run("desc").checktable();
		kindPrice = (float) desc.rawget("price").optdouble(15);
	}

	@Override
	public String getEntityKind() {
		return potionKind;
	}

	@NotNull
	static public Scroll createRandomScroll(){
		ArrayList<String> inscribable = new ArrayList<>(inscribableScrolls);
		return (Scroll) ItemFactory.itemByName(inscribable.get(Random.Int(inscribable.size())));
	}

	@Override
	public ArrayList<String> actions(Char hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( CommonActions.AC_READ );
		return actions;
	}

	@Override
	public void _execute(@NotNull Char chr, @NotNull String action ) {
		if (action.equals( CommonActions.AC_READ )) {
			if (chr.hasBuff( BuffFactory.BLINDNESS )) {
				GLog.w(StringsManager.getVar(R.string.Scroll_Blinded));
			} else {
				detach( chr.getBelongings().backpack );
				doRead(chr);
			}
			chr.readyAndIdle();
		} else {
			super._execute(chr, action );
		}
	}

	// the read effect lives in scripts/items/<Kind>.lua (hook doRead); the two
	// grid-sweep reads (magic mapping, summoning) call their named java
	// helpers from there
	protected void doRead(@NotNull Char reader) {
		if (kindScript != null) {
			kindScript.run("doRead", reader);
		}
	}

	// ScrollOfMagicMapping effect: map the whole level, reveal secrets
	// (was ScrollOfMagicMapping.doRead - java-shaped level array mutation)
	@LuaInterface
	public void magicMappingRead(@NotNull Char reader) {
		Level level = reader.level();
		int length = level.getLength();
		int[] map = level.map;
		boolean[] mapped = level.mapped;
		boolean[] discoverable = level.discoverable;
		boolean noticed = false;
		for (int i = 0; i < length; i++) {
			int terr = map[i];
			if (discoverable[i]) {
				mapped[i] = true;
				if ((TerrainFlags.flags[terr] & TerrainFlags.SECRET) != 0) {
					level.set(i, Terrain.discover(terr));
					GameScene.updateMap(i);
					if (Dungeon.isCellVisible(i)) {
						GameScene.discoverTile(i);
						discoverStatic(i);
						noticed = true;
					}
				}
			}
		}
		for (var lo : level.getAllLevelObjects()) {
			if (lo.secret()) {
				lo.discover();
				discoverStatic(lo.getPos());
				noticed = true;
			}
		}
		reader.observe();
		GLog.i(StringsManager.getVar(R.string.ScrollOfMagicMapping_Layout));
		if (noticed) {
			Sample.INSTANCE.play(Assets.SND_SECRET);
		}
		SpellSprite.show(reader, SpellSprite.MAP);
		Sample.INSTANCE.play(Assets.SND_READ);
		CharUtils.dispelInvisibility(reader);
		setKnown();
		reader.spend(TIME_TO_READ);
	}

	// was ScrollOfMagicMapping.discover - the search-jaunt sparkle
	@LuaInterface
	public static void discoverStatic(int cell) {
		com.watabou.pixeldungeon.effects.CellEmitter.get(cell)
				.start(Speck.factory(Speck.DISCOVER), 0.1f, 4);
	}

	// ScrollOfSummoning effect: a random (pettable) mob appears beside the
	// reader (was ScrollOfSummoning.doRead)
	@LuaInterface
	public void summoningRead(@NotNull Char reader) {
		Level level = Dungeon.level;
		if (level.isBossLevel() || !level.cellValid(level.randomRespawnCell())) {
			GLog.w(Utils.format(R.string.Using_Failed_Because_Magic, name()));
			return;
		}
		int cell = level.getEmptyCellNextTo(reader.getPos());
		if (level.cellValid(cell)) {
			Mob mob = Bestiary.mob(level);
			GLog.i(StringsManager.getVar(R.string.ScrollOfSummoning_Info_2));
			if (mob.canBePet()) {
				Mob.makePet(mob, reader.getId());
			} else {
				GLog.w(Utils.format(R.string.Mob_Cannot_Be_Pet, mob.getName()));
			}
			WandOfBlink.appear(mob, cell);
		} else {
			GLog.w(StringsManager.getVar(R.string.No_Valid_Cell));
		}
		setKnown();
		SpellSprite.show(reader, SpellSprite.SUMMON);
		Sample.INSTANCE.play(Assets.SND_READ);
		CharUtils.dispelInvisibility(reader);
		reader.spend(TIME_TO_READ);
	}

	@LuaInterface
	public boolean isKnown() {
		return handler.isKnown( potionKind );
	}

	@LuaInterface
	public void setKnown() {
		if (!isKnown()) {
			handler.know( potionKind );
		}

		Badges.validateAllScrollsIdentified();
	}

	@Override
	public Item identify() {
		setKnown();
		return super.identify();
	}

	@Override
	public String name() {
		return isKnown() ? name : Utils.format(R.string.Scroll_Name, rune);
	}

	@Override
	public String info() {
		return isKnown() ? desc() : Utils.format(R.string.Scroll_Info, rune);
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return isKnown();
	}

	public static HashSet<String> getKnown() {
		return handler.known();
	}

	public static HashSet<String> getUnknown() {
		return handler.unknown();
	}

	public static boolean allKnown() {
		return handler.allKnown();
	}

	@Override
	public int price() {
		return (int) ((isKnown() ? kindPrice : 15) * quantity());
	}

	@Override
	public Item burn(int cell){
		return null;
	}

	@Override
	public String bag() {
		return ScrollHolder.class.getSimpleName();
	}

	// --- shared scroll helpers moved here from the per-scroll classes ---

	// was ScrollOfRemoveCurse.uncurseItem
	@LuaInterface
	public static boolean uncurseItem(boolean procced, Item item) {
		if (item != null && item.isCursed()) {
			item.setCursed(false);
			procced = true;
		}
		return procced;
	}

	// was ScrollOfRemoveCurse.uncurse(Belongings)
	@LuaInterface
	public static boolean uncurseAll(@NotNull Char reader) {
		Belongings belongings = reader.getBelongings();
		Iterator<Item> itemIterator = belongings.iterator();
		boolean procced = false;
		while (itemIterator.hasNext()) {
			procced = uncurseItem(procced, itemIterator.next());
		}
		return procced;
	}

	// was ScrollOfRemoveCurse.uncurse(hero, item) single-item form with fx
	@LuaInterface
	public static void uncurseAndFx(@NotNull Char hero, Item item) {
		boolean procced = uncurseItem(false, item);
		if (procced) {
			hero.getSprite().emitter().start(ShadowParticle.UP, 0.05f, 10);
		}
	}

	// was ScrollOfCurse.curse(hero, item) single-item form
	@LuaInterface
	public static void curseItem(@NotNull Char hero, Item item) {
		if (!item.isCursed()) {
			item.setCursed(true);
			item.setCursedKnown(true);
			if (item.isCursed()) {
				hero.getSprite().emitter().start(ShadowParticle.UP, 0.05f, 10);
			}
		}
	}

	// was ScrollOfIdentify.identify
	@LuaInterface
	public static void identifyItem(@NotNull Char ch, @NotNull Item item) {
		com.watabou.pixeldungeon.effects.Identification identification =
				new com.watabou.pixeldungeon.effects.Identification(ch.getSprite().center().offset(0, -16));
		GameScene.addToMobLayer(identification);
		item.identify();
		GLog.i(Utils.format(R.string.ScrollOfIdentify_Info1, item));
		Badges.validateItemLevelAcquired(item);
	}

	// was ScrollOfUpgrade.upgrade(hero) - upgrade sparkle
	@LuaInterface
	public static void upgradeFx(@NotNull Char hero) {
		hero.getSprite().emitter().start(Speck.factory(Speck.UP), 0.2f, 3);
	}

	// was ScrollOfRecharging.charge(hero) - recharge sparkle
	@LuaInterface
	public static void chargeFx(@NotNull Char hero) {
		hero.getSprite().centerEmitter().burst(EnergyParticle.FACTORY, 15);
	}
}
