
package com.watabou.pixeldungeon.items.food;

import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.levels.objects.LevelObject;
import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.mechanics.LuaScript;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.levels.Level;
import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.LuaValue;

/**
 * Data food: one java kind-parameterized framework, per-kind data and behavior
 * in scripts/items/<Kind>.lua built with scripts/lib/food.lua (was 12 per-food
 * java classes). Kind == the legacy class simple name, so name/info string
 * resources and old saves (entityKind / derived legacy class tag) resolve
 * unchanged.
 *
 * Lua hooks (all optional, self = the script table):
 *   desc(self)                 -> { image, imageFile, energy, message, price } (data, read once per kind)
 *   eat(self, item, chr)       -> replaces the java eat entirely (PseudoPasty)
 *   onExecute(self,item,chr,action) -> extra effects after any executed action,
 *                                 same shape as the old _execute overrides
 *   pick(self, item, ch, pos)  -> replaces Item.pick (mimic traps)
 *   burn/freeze/poison(self, item, cell) -> kind string to morph into, nil = default
 *   purify(self, item)         -> kind string of the fresh food, nil = itself
 *   molder(self, item, cell)   -> presence marks the kind rotten (moisten/purify UX)
 */
public class Food extends Item {

	public static final float TIME_TO_EAT	= 3f;

	// data food kinds (kind == legacy class simple name: string resources,
	// save entityKind and legacy class tags all resolve through it)
	public static final String RATION            = "Ration";
	public static final String OVERPRICED_RATION = "OverpricedRation";
	public static final String PASTY             = "Pasty";
	public static final String PSEUDO_PASTY      = "PseudoPasty";
	public static final String MYSTERY_MEAT      = "MysteryMeat";
	public static final String CHARGRILLED_MEAT  = "ChargrilledMeat";
	public static final String FROZEN_CARPACCIO  = "FrozenCarpaccio";
	public static final String ROTTEN_MEAT       = "RottenMeat";
	public static final String ROTTEN_PASTY      = "RottenPasty";
	public static final String ROTTEN_RATION     = "RottenRation";
	public static final String CANDY_OF_DEATH    = "CandyOfDeath";
	public static final String PUMPKIN_PIE       = "PumpkinPie";
	public static final String ROTTEN_PUMPKIN_PIE = "RottenPumpkinPie";
	public static final String CHRISTMAS_TURKEY  = "ChristmasTurkey";
	public static final String CANDY             = "Candy";

	public float energy   = 0;
	public String message = StringsManager.getVar(R.string.Food_Message);

	private float kindPrice = -1; // -1 = not sellable (authored price 0 stays 0, Item price conventions)

	@Nullable
	protected final String itemKind;

	// per-instance wrapper around the shared per-kind module (LuaEngine.require
	// caches the module; no asInstance() = no per-instance lua state). parent =
	// this item, so hooks follow the lib/item convention (self, item, ...).
	@Nullable
	private final LuaScript kindScript;

	{
		stackable = true;
		setDefaultAction(CommonActions.AC_EAT);
	}

	// ancient-save target: com.watabou...food.Food alias resolves here, those
	// items were plain rations (android RemixedDungeon alias table, remix 0.5)
	public Food() {
		this("Ration");
	}

	public Food(@NotNull String kind) {
		itemKind = kind;
		kindScript = new LuaScript("scripts/items/" + kind, this);
		applyKind();
	}

	private void applyKind() {
		// Item's name/info fields initialize before any ctor body runs, i.e.
		// before itemKind is set - re-resolve them now that the kind is known
		name = getClassParam("Name", StringsManager.getVar(R.string.Item_Name), false);
		info = getClassParam("Info", StringsManager.getVar(R.string.Item_Info), false);

		LuaScript script = kindScript;
		if (script == null || !script.hasMethod("desc")) {
			return;
		}
		LuaValue desc = script.run("desc").checktable();
		energy       = (float) desc.rawget("energy").optdouble(0);
		message      = StringsManager.maybeId(desc.rawget("message").optjstring(message));
		kindPrice    = (float) desc.rawget("price").optdouble(-1);
		int img      = desc.rawget("image").optint(-1);
		if (img >= 0) {
			image = img;
		}
		String imgFile = desc.rawget("imageFile").optjstring(null);
		if (imgFile != null) {
			imageFile = imgFile;
		}
	}

	@Override
	public String getEntityKind() {
		return itemKind != null ? itemKind : super.getEntityKind();
	}

	@Override
	public ArrayList<String> actions(Char hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( CommonActions.AC_EAT );
		return actions;
	}

	@Override
	public void _execute(@NotNull Char chr, @NotNull String action ) {
		if (action.equals( CommonActions.AC_EAT )) {
			LuaScript script = kindScript;
			if (script != null && script.hasMethod("eat")) {
				script.run("eat", chr);
				return; // lua owns the whole eat (PseudoPasty), no onExecute tail
			}
			chr.eat(this, energy, message);
		} else {
			super._execute(chr, action );
		}

		LuaScript script = kindScript;
		if (script != null && script.hasMethod("onExecute")) {
			script.run("onExecute", chr, action);
		}
	}

	@Override
	public Item pick(Char ch, int pos) {
		LuaScript script = kindScript;
		if (script != null && script.hasMethod("pick")) {
			LuaValue ret = script.run("pick", ch, pos);
			if (!ret.isnil()) {
				return (Item) ret.checkuserdata(Item.class);
			}
		}
		return super.pick(ch, pos);
	}

	@Override
	protected void onThrow(int cell, @NotNull Char thrower, Char enemy) {
		LuaScript script = kindScript;
		if (script != null && script.hasMethod("molder")) {
			// rotten kinds: the gas burst eats the thrown item 3/4 of the time
			// (was RottenFood.onThrow; the molder hook answers "gas spawned?")
			Level level = thrower.level();
			LevelObject lo = level.getTopLevelObject(cell);
			if ((lo != null && lo.affectItems()) || level.pit[cell]) {
				super.onThrow(cell, thrower, enemy);
			} else if (!script.run("molder", cell).toboolean()) {
				super.onThrow(cell, thrower, enemy);
			}
			return;
		}
		super.onThrow(cell, thrower, enemy);
	}

	private Item morphVia(@NotNull String hook, int cell) {
		LuaScript script = kindScript;
		if (script != null && script.hasMethod(hook)) {
			LuaValue ret = script.run(hook, cell);
			if (ret.isstring()) {
				Item result = ItemFactory.itemByName(ret.tojstring());
				result.quantity(quantity());
				return result;
			}
		}
		return super.burn(cell); // all three java defaults are identity
	}

	@Override
	public Item burn(int cell) {
		return morphVia("burn", cell);
	}

	@Override
	public Item freeze(int cell) {
		return morphVia("freeze", cell);
	}

	@Override
	public Item poison(int cell) {
		return morphVia("poison", cell);
	}

	@Override
	public int price() {
		if (kindPrice < 0) {
			return 0;
		}
		return (int) kindPrice * quantity();
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	// rotten kinds keep purify reachable from java (PotionOfPurity moisten path);
	// the fresh target is authored in the script
	public Food purify() {
		LuaScript script = kindScript;
		if (script != null && script.hasMethod("purify")) {
			LuaValue ret = script.run("purify");
			if (ret.isstring()) {
				return (Food) ItemFactory.itemByName(ret.tojstring());
			}
		}
		return this;
	}

	// rotten kinds mark themselves via the molder hook; java UX (moisten
	// selector, item button enable) only asks the boolean
	public boolean isRotten() {
		return kindScript != null && kindScript.hasMethod("molder");
	}
}
