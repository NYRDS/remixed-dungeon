
package com.watabou.pixeldungeon.items.potions;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.items.common.UnknownItem;
import com.nyrds.pixeldungeon.levels.objects.LevelObject;
import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.mechanics.LuaScript;
import com.nyrds.pixeldungeon.mechanics.buffs.BuffFactory;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.ItemsList;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.gfx.BitmapData;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.Freezing;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.effects.Splash;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.ItemStatusHandler;
import com.watabou.pixeldungeon.items.ItemStatusHandlerKinds;
import com.watabou.pixeldungeon.items.food.Food;
import com.watabou.pixeldungeon.items.scrolls.Scroll;
import com.watabou.pixeldungeon.items.weapon.missiles.Arrow;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.utils.BArray;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndBag;
import com.watabou.pixeldungeon.windows.WndOptions;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import java.util.ArrayList;
import java.util.HashSet;
import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.LuaValue;

/**
 * Data potions: one java kind-parameterized framework, per-kind data and
 * behavior in scripts/items/<Kind>.lua built with scripts/lib/potion.lua
 * (was 13 per-potion java classes). Kind == the legacy class simple name, so
 * name/info resources and old saves resolve unchanged; the identification
 * handler keeps the legacy "class <pkg>.<Kind>" bundle keys verbatim.
 *
 * Lua hooks (all optional, self = the script table, item = the potion):
 *   desc(self)                 -> { labelIndex, price }   (data, once per kind)
 *   apply(self, item, chr)     -> drink effect (nil = base shatter fallback)
 *   shatter(self, item, cell)  -> thrown-break effect (nil = base splash)
 *   moistenArrow(self, item, arrow, owner)      -> arrow kind string it turns into
 *   moistenScroll(self, item, scroll, owner)    -> replacement scroll kind string
 *   moistenRottenFood(self, item, food, owner)  -> "detached" to fly away (truthy)
 */
public class Potion extends Item implements UnknownItem {

	public static final String HEALING        = "PotionOfHealing";
	public static final String EXPERIENCE     = "PotionOfExperience";
	public static final String TOXIC_GAS      = "PotionOfToxicGas";
	public static final String LIQUID_FLAME   = "PotionOfLiquidFlame";
	public static final String STRENGTH       = "PotionOfStrength";
	public static final String PARALYTIC_GAS  = "PotionOfParalyticGas";
	public static final String LEVITATION     = "PotionOfLevitation";
	public static final String MIND_VISION    = "PotionOfMindVision";
	public static final String PURITY         = "PotionOfPurity";
	public static final String INVISIBILITY   = "PotionOfInvisibility";
	public static final String MIGHT          = "PotionOfMight";
	public static final String FROST          = "PotionOfFrost";
	public static final String MANA           = "PotionOfMana";

	private static final String AC_MOISTEN = "Potion_ACMoisten";
	
	private static final float TIME_TO_DRINK = 1f;
	private static final float TIME_TO_MOISTEN = 1f;

	private static final int DISTANCE_SWEEP = 2;

	// legacy class package: identification bundle keys keep this exact form so
	// per-class-era saves and kind-era saves stay interchangeable
	private static final String LEGACY_PACKAGE = "com.watabou.pixeldungeon.items.potions";

	private static final String[] potions = {
		HEALING,
		EXPERIENCE,
		TOXIC_GAS,
		LIQUID_FLAME,
		STRENGTH,
		PARALYTIC_GAS,
		LEVITATION,
		MIND_VISION,
		PURITY,
		INVISIBILITY,
		MIGHT,
		FROST,
		MANA
	};

	// known-harmful when identified: default action is throw, drink asks twice
	private static final HashSet<String> harmfulKinds = new HashSet<>(
			java.util.Arrays.asList(LIQUID_FLAME, TOXIC_GAS, PARALYTIC_GAS, FROST));

	// known-beneficial: throwing them away asks twice
	private static final HashSet<String> beneficialThrowKinds = new HashSet<>(
			java.util.Arrays.asList(EXPERIENCE, HEALING, LEVITATION, MIND_VISION, STRENGTH,
					INVISIBILITY, MIGHT, MANA));

	private static final Integer[] images;

	private static ItemStatusHandlerKinds handler;
	
	private final String color;

	protected int labelIndex = -1;

	@NotNull
	protected final String potionKind;

	// per-instance wrapper around the shared per-kind module (see Food)
	@NotNull
	private final LuaScript kindScript;

	static {
		images = new Integer[13];
		for (int i = 0;i<13;i++) {
			images[i] = 32 + i;
		}
	}

	// potions are constructed before Dungeon.init in some boot paths
	// (alchemy recipe validation), and the ctor's knownHamful() would NPE
	// on a null handler; self-init like Ring does, Dungeon.init/restore
	// still replace this with the per-run handler afterwards
	static {
		initColors();
	}

	public static final String ITEMS_POTIONS_PNG = "items/potions.png";

	{
		stackable = true;
		setDefaultAction(CommonActions.AC_DRINK);
		imageFile = ITEMS_POTIONS_PNG;
	}
	
	
	private boolean shattered = false;

	public static void initColors() {
		handler = new ItemStatusHandlerKinds(LEGACY_PACKAGE, potions, images);
	}
	
	public static void save( Bundle bundle ) {
		handler.save( bundle );
	}
	
	public static void restore( Bundle bundle ) {
		handler = new ItemStatusHandlerKinds(LEGACY_PACKAGE, potions, images, bundle);
	}
	
	// restore fallback for the bare legacy tag; plain potions were rations of
	// the potion world - healing is the canonical one
	public Potion() {
		this(HEALING);
	}

	public Potion(@NotNull String kind) {
		potionKind = kind;
		kindScript = new LuaScript("scripts/items/" + kind, this);
		if (handler != null) {
			image = handler.index( potionKind );
		}
		color = StringsManager.getVars(R.array.Potion_Colors)[ItemStatusHandler.indexByImage(image, images)];
		applyKind();
		if(knownHamful()) {
			setDefaultAction(AC_THROW);
		}
	}

	private void applyKind() {
		// Item's name/info initialize before the kind is known - re-resolve
		name = getClassParam("Name", StringsManager.getVar(R.string.Item_Name), false);
		info = getClassParam("Info", StringsManager.getVar(R.string.Item_Info), false);

		if (!kindScript.hasMethod("desc")) {
			return;
		}
		LuaValue desc = kindScript.run("desc").checktable();
		labelIndex = desc.rawget("labelIndex").optint(-1);
		kindBasePrice = (float) desc.rawget("price").optdouble(20);
	}

	@Override
	public String getEntityKind() {
		return potionKind;
	}
	
	@Override
	public ArrayList<String> actions(Char hero ) {
		if(knownHamful()) {
			setDefaultAction(AC_THROW);
		}

		ArrayList<String> actions = super.actions( hero );
		actions.add(CommonActions.AC_DRINK);
		actions.add(AC_MOISTEN);
		return actions;
	}
	
	@Override
	public void _execute(@NotNull final Char chr, @NotNull String action ) {
		switch (action) {
			case CommonActions.AC_DRINK:
				if (knownHamful()) {

                    GameScene.show(
							new WndOptions(StringsManager.getVar(R.string.Potion_Harmfull),
                                    StringsManager.getVar(R.string.Potion_SureDrink),
                                    StringsManager.getVar(R.string.Potion_Yes),
                                    StringsManager.getVar(R.string.Potion_No)) {
								@Override
								public void onSelect(int index) {
									if (index == 0) {
										drink(chr);
									}
								}
							}
					);

				} else {
					drink(chr);
				}

				break;
			case AC_MOISTEN:
				moisten(chr);
				break;
			default:

				super._execute(chr, action);

				break;
		}
	}

	public boolean knownHamful() {
		return isKnown() && harmfulKinds.contains(potionKind);
	}

	@Override
	public void doThrow(@NotNull final Char chr) {

		if (isKnown() && beneficialThrowKinds.contains(potionKind)) {

            GameScene.show(
				new WndOptions(StringsManager.getVar(R.string.Potion_Beneficial),
                        StringsManager.getVar(R.string.Potion_SureThrow),
                        StringsManager.getVar(R.string.Potion_Yes),
                        StringsManager.getVar(R.string.Potion_No)) {
					@Override
					public void onSelect(int index) {
						if (index == 0) {
							Potion.super.doThrow(chr);
						}
					}
				}
			);
			
		} else {
			super.doThrow(chr);
		}
	}
	
	protected void drink(Char hero ) {
		
		detach( hero.getBelongings().backpack );

		onThrow( hero.getPos(), hero, hero);
		
		Sample.INSTANCE.play( Assets.SND_DRINK );
		
		hero.doOperate(TIME_TO_DRINK);
		shattered = false;
	}
	
	private void moisten(Char hero) {
        GameScene.selectItem(hero, itemSelector, WndBag.Mode.MOISTABLE, StringsManager.getVar(R.string.Potion_SelectForMoisten));
		hero.doOperate(TIME_TO_MOISTEN);
	}
	
	@Override
	protected void onThrow(int cell, @NotNull Char thrower, Char enemy) {
		if (thrower.getPos() == cell) {
			apply( thrower );
		} else {
			Level level = thrower.level();
			if (level.pit[cell]) {
				super.onThrow( cell, thrower, enemy);
			} else  {
				if (isMagicWell(level, cell)) {
					// magic wells take the potion whole: it lands, then the water
					// identifies / transmutes / tosses it out - no shatter (beta.10 round)
					level.drop(this, cell);
				} else {
					LevelObject lo = level.getTopLevelObject(cell);
					if (lo != null && lo.affectItems()) {
						lo.bump(this); //traps and the like still react, but the potion breaks
					}
					shatter( cell );
				}
			}
		}
	}

	private boolean isMagicWell(Level level, int cell) {
		return level.blobAmountAt("WaterOfAwareness", cell) > 0
				|| level.blobAmountAt("WaterOfHealth", cell) > 0
				|| level.blobAmountAt("WaterOfTransmutation", cell) > 0;
	}
	
	protected void apply(Char hero ) {
		if (kindScript.hasMethod("apply")) {
			kindScript.run("apply", hero);
			return;
		}
		shatter( hero.getPos() );
	}

	protected boolean canShatter() {
		if(!shattered) {
			shattered = true;
			return true;
		}
		return false;
	}

	public void shatter( int cell ) {
		if (kindScript.hasMethod("shatter")) {
			kindScript.run("shatter", cell);
			return;
		}
        GLog.i(Utils.format(R.string.Potion_Shatter, color()));
		Sample.INSTANCE.play( Assets.SND_SHATTER );
		splash( cell );
	}
	
	public boolean isKnown() {
		return handler.isKnown( potionKind );
	}
	
	@LuaInterface
	public void setKnown() {
		if (!isKnown()) {
			handler.know( potionKind );
		}
		
		Badges.validateAllPotionsIdentified();
	}
	
	@Override
	public Item identify() {
		setKnown();
		return this;
	}
	
	@LuaInterface
	public String color() {
		return color;
	}
	
	@Override
	public String name() {
        return isKnown() ? name : Utils.format(R.string.Potion_Name, color);
	}
	
	@Override
	public String info() {
        return isKnown() ? desc() : Utils.format(R.string.Potion_Info, color);
	}
	
	@Override
	public boolean isIdentified() {
		return isKnown();
	}
	
	@Override
	public boolean isUpgradable() {
		return false;
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

	// the canonical healing-drink effect, kept reachable from java
	// (ChaosStaff crit heals; was PotionOfHealing.heal)
	private static final Potion healShim = new Potion(HEALING);

	public static void heal(Char ch, float portion) {
		ch.heal((int) (ch.ht() * portion), healShim);
		Buff.detach(ch, BuffFactory.POISON);
		Buff.detach(ch, BuffFactory.CRIPPLE);
		Buff.detach(ch, BuffFactory.WEAKNESS);
		Buff.detach(ch, BuffFactory.BLEEDING);
	}

	static private final int[] splashColors = new int[13];
	static {
		BitmapData bmp = ModdingMode.getBitmapData(ITEMS_POTIONS_PNG);
		int rows = bmp.getWidth() / ItemSprite.SIZE;
		for (int i = 0; i < splashColors.length; i++) {
			int row = i / rows;
			int col = i % rows;
			splashColors[i] = bmp.getPixel(col * ItemSprite.SIZE + 8, row * ItemSprite.SIZE + 10);
		}
	}
	@LuaInterface
	public void splash( int cell ) {
		if(!GameScene.isSceneReady()) {
			return;
		}

		Splash.at( cell, splashColors[ItemStatusHandler.indexByImage(image, images)], 5 );
	}

	private float kindBasePrice = 20;

	public int basePrice() {
		return (int) kindBasePrice;
	}

	@Override
	public int price() {
		return (int) ((isKnown() ? basePrice() : 20) * quantity() * qualityFactor());
	}
	
	@Override
	public Item freeze(int cell){
		shatter(cell);
		return null;
	}
	
	private final WndBag.Listener itemSelector = (item, selector) -> {
		if (item != null) {

			if(item instanceof Arrow) {
				moistenArrow ((Arrow) item, item.getOwner());
			}

			if(item instanceof Scroll) {
				moistenScroll ((Scroll) item, item.getOwner());
			}

			if(item instanceof Food && ((Food) item).isRotten()) {
				moistenRottenFood((Food) item, item.getOwner());
			}
		}
	};

	@LuaInterface
	public int detachMoistenItems(Item item, int maxQuantity) {
		setKnown();
		int quantity = item.quantity();
		
		if(quantity <= maxQuantity){
			Char owner = getOwner();

			if(item.equals(owner.getItemFromSlot(Belongings.Slot.WEAPON))) {
				owner.getBelongings().setItemForSlot(ItemsList.DUMMY, Belongings.Slot.WEAPON);
			} else {
				item.detachAll( owner.getBelongings().backpack );
			}
		} else {
			item.quantity(item.quantity() - maxQuantity);
			quantity = maxQuantity;
		}
		return quantity;
	}

	@LuaInterface
	public double qualityFactor() {
		return 1;
	}

	// PotionOfPurity shatter: clear Toxic/Paralytic/Confusion gas within 2 cells
	// of the impact (was PotionOfPurity.shatter's pathfinder sweep)
	@LuaInterface
	public void purityShatter(int cell) {
		Level level = getOwner().level();
		PathFinder.buildDistanceMap(cell, BArray.not(level.losBlocking, null), (int) (DISTANCE_SWEEP * qualityFactor()));
		boolean procd = false;
		Blob[] blobs = {
				level.blobAt("ToxicGas"),
				level.blobAt("ParalyticGas"),
				level.blobAt("ConfusionGas")
		};
		for (Blob blob : blobs) {
			if (blob == null) {
				continue;
			}
			for (int i = 0; i < level.getLength(); i++) {
				if (PathFinder.distance[i] < Integer.MAX_VALUE) {
					int value = blob.cur[i];
					if (value > 0) {
						blob.cur[i] = 0;
						blob.setVolume(blob.getVolume() - value);
						procd = true;
						CellEmitter.get(i).burst(Speck.factory(Speck.DISCOVER), 1);
					}
				}
			}
		}
		boolean heroAffected = PathFinder.distance[Dungeon.hero.getPos()] < Integer.MAX_VALUE;

		if (procd) {
			splash(cell);
			Sample.INSTANCE.play(Assets.SND_SHATTER);
			setKnown();
			if (heroAffected) {
				GLog.p(StringsManager.getVar(R.string.PotionOfPurity_Freshness));
			}
		} else {
			shatter(cell); // the base no-hook shatter path is unreachable here (hook present);
			               // mirror the old super.shatter behavior instead
			GLog.i(Utils.format(R.string.Potion_Shatter, color()));
			Sample.INSTANCE.play(Assets.SND_SHATTER);
			splash(cell);
			if (heroAffected) {
				GLog.i(StringsManager.getVar(R.string.PotionOfPurity_Freshness));
				setKnown();
			}
		}
	}

	// PotionOfFrost shatter: freeze everything within 2 cells of the impact
	// (was PotionOfFrost.shatter's pathfinder sweep)
	@LuaInterface
	public void frostShatter(int cell) {
		if (!canShatter()) {
			return;
		}
		Level level = Dungeon.level;
		PathFinder.buildDistanceMap(cell, BArray.not(level.losBlocking, null), (int) (DISTANCE_SWEEP * qualityFactor()));
		for (int i = 0; i < level.getLength(); i++) {
			if (PathFinder.distance[i] < Integer.MAX_VALUE) {
				Freezing.affect(i);
			}
		}
		splash(cell);
		Sample.INSTANCE.play(Assets.SND_SHATTER);
		setKnown();
	}

	protected int reallyMoistArrows(Arrow arrow,Char owner) {
		int quantity = detachMoistenItems(arrow, (int) (10*qualityFactor()));
		moistenEffective(owner);
        GLog.i(StringsManager.getVar(R.string.Potion_ArrowMoisten));
		return quantity;
	}

	protected void moistenRottenFood(Food rottenFood, Char owner) {
		if (kindScript.hasMethod("moistenRottenFood")) {
			kindScript.run("moistenRottenFood", rottenFood, owner);
			return;
		}
		moistenUseless(owner);
	}
	
	protected void moistenScroll(Scroll scroll, Char owner) {
		if (kindScript.hasMethod("moistenScroll")) {
			// truthy lua return = replacement scroll kind string
			LuaValue ret = kindScript.run("moistenScroll", scroll, owner);
			if (ret.isstring()) {
				int quantity = detachMoistenItems(scroll, scroll.quantity());
				Item replacement = ItemFactory.itemByName(ret.tojstring());
				replacement.quantity(quantity);
				owner.collect(replacement);
				return;
			}
			return; // lua handled it (fly-away etc.)
		}
		moistenUseless(owner);
	}
	
	protected void moistenArrow(Arrow arrow, Char owner) {
		if (kindScript.hasMethod("moistenArrow")) {
			LuaValue ret = kindScript.run("moistenArrow", arrow, owner);
			if (ret.isstring()) {
				int quantity = reallyMoistArrows(arrow, owner);
				Item moistened = ItemFactory.itemByName(ret.tojstring());
				moistened.quantity(quantity);
				owner.collect(moistened);
				return;
			}
			return; // lua handled it
		}
		moistenUseless(owner);
	}
	
	private void moistenUseless(@NotNull Char owner) {

		detach(owner.getBelongings().backpack );
        GLog.i(StringsManager.getVar(R.string.Potion_MoistenUseless));
		owner.doOperate(TIME_TO_MOISTEN );
	}
	
	@LuaInterface
	public void moistenEffective(@NotNull Char owner) {
		detach(owner.getBelongings().backpack );
		identify();
		owner.doOperate(TIME_TO_MOISTEN );
	}

	@Override
	public int overlayIndex() {
		if(!isIdentified()) {
			return super.overlayIndex();
		}
		return labelIndex;
	}

	@Override
	public String bag() {
		return "PotionBelt";
	}
}
