package com.watabou.pixeldungeon.plants;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.levels.objects.LevelObjectsFactory;
import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.mechanics.LuaScript;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.CharsList;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.effects.SpellSprite;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.bags.Bag;
import com.watabou.pixeldungeon.items.food.Food;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.TerrainFlags;
import com.watabou.pixeldungeon.utils.Utils;
import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.LuaValue;

/**
 * Data seed: one java kind-parameterized framework, per-kind data and behavior
 * in scripts/items/<Kind>.lua built with scripts/lib/seed.lua (was 9 per-seed
 * java classes). Kind == "<Plant>.Seed", so Treasury/alchemy/quest references
 * and old saves resolve unchanged; legacy "...plants.<Plant>$Seed" FQNs route
 * through Bundle.addKindAlias (ItemFactory).
 *
 * The kind script loads per-instance (asInstance): the kind filenames carry a
 * dot ("Sungrass.Seed.lua"), and require's searchpath maps every dot to a
 * separator, so the shared-module require path cannot reach them - dofile
 * takes the name verbatim. Hooks stay stateless (seeds are stackable).
 *
 * Lua hooks (all optional, self = the per-instance script table):
 *   desc(self)                 -> { plantName, descText, image, alchemyKind }
 *                                 (data, maybeId'd string ids, read once per
 *                                 instance; missing fields keep java defaults)
 *   onEat(self, item, chr)     -> effects after the eat plumbing (detach,
 *                                 time, sprite, sound), same shape as the old
 *                                 _execute AC_EAT overrides
 *   onCollect(self, item, bag) -> post-collect notification (Rotberry
 *                                 challenge), runs only when collect succeeded
 *   inflammable(self, item, cell) -> true keeps the seed out of the fire
 *                                 (Rotberry), false/absent burns it to nothing
 */
public class Seed extends Item {

	public static final String AC_PLANT = "Plant_ACPlant";

	private static final float TIME_TO_PLANT = 1f;

	private static final String SEED_SUFFIX = ".Seed";

	{
		stackable = true;
		setDefaultAction(AC_THROW);
		imageFile = "items/seeds.png";
	}

	// entity kind of the plant this seed grows ("Firebloom", ...), resolved
	// through LevelObjectsFactory
	@Nullable
	protected String plantKind;
	@Nullable
	protected String plantName;

	// entity kind of the potion this seed brews in the alchemy pot
	@LuaInterface
	@Nullable
	public String alchemyKind;

	@Nullable
	protected final String itemKind;

	private String kindDesc = StringsManager.getVar(R.string.Item_Info);

	@Nullable
	private final LuaScript kindScript;

	public Seed(@NotNull String kind) {
		itemKind = kind;
		plantKind = kind.substring(0, kind.length() - SEED_SUFFIX.length());

		String scriptPath = "scripts/items/" + kind;
		if (ModdingMode.isResourceExists(scriptPath + ".lua")) {
			kindScript = new LuaScript(scriptPath, this);
			kindScript.asInstance();
		} else {
			kindScript = null;
		}
		applyKind();
	}

	private void applyKind() {
		LuaScript script = kindScript;
		if (script == null || !script.hasMethod("desc")) {
			return;
		}
		LuaValue desc = script.run("desc").checktable();
		plantName   = StringsManager.maybeId(desc.rawget("plantName").optjstring(null));
		alchemyKind = desc.rawget("alchemyKind").optjstring(null);
		kindDesc    = StringsManager.maybeId(desc.rawget("descText").optjstring(kindDesc));

		int img = desc.rawget("image").optint(-1);
		if (img >= 0) {
			image = img;
		}

		if (plantName != null) {
			name = Utils.format(R.string.Plant_Seed, plantName);
		}
	}

	@Override
	public String getEntityKind() {
		// itemKind is still null during Item field init (the name/info
		// getClassParam lookups run before the ctor arg lands) - "Seed"
		// satisfies those without the class-registry reverse lookup, which
		// has no framework entry to offer
		return itemKind != null ? itemKind : "Seed";
	}

	@Override
	public ArrayList<String> actions(Char hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_PLANT);
		actions.add(CommonActions.AC_EAT);
		return actions;
	}

	@Override
	protected void onThrow(int cell, @NotNull Char thrower, Char enemy) {
		Level level = thrower.level();

		if (level.pit[cell] || level.getTopLevelObject(cell) != null || !TerrainFlags.is(level.map[cell],TerrainFlags.PASSABLE)) {
			super.onThrow(cell, thrower, enemy);
		} else {
			level.plant(this, cell);
		}
	}

	@Override
	public void _execute(@NotNull Char chr, @NotNull String action) {
		if (action.equals(AC_PLANT)) {
			((Seed) detach(chr.getBelongings().backpack)).onThrow(chr.getPos(), chr, CharsList.DUMMY);
			chr.doOperate(TIME_TO_PLANT);
		} else if (action.equals(CommonActions.AC_EAT)) {
			detach(chr.getBelongings().backpack);

			chr.doOperate(Food.TIME_TO_EAT);

			SpellSprite.show(chr, SpellSprite.FOOD);
			Sample.INSTANCE.play(Assets.SND_EAT);
		}

		super._execute(chr, action);

		LuaScript script = kindScript;
		if (script != null && script.hasMethod("onEat")) {
			script.run("onEat", chr);
		}
	}

	@Override
	public boolean collect(@NotNull Bag container) {
		boolean collected = super.collect(container);

		LuaScript script = kindScript;
		if (collected && script != null && script.hasMethod("onCollect")) {
			script.run("onCollect", container);
		}
		return collected;
	}

	@Override
	public Item burn(int cell) {
		LuaScript script = kindScript;
		if (script != null && script.hasMethod("inflammable") && script.run("inflammable", cell).toboolean()) {
			return this;
		}
		return null;
	}

	public Plant couch(int pos) {
		Sample.INSTANCE.play(Assets.SND_PLANT);
		Plant plant = (Plant) LevelObjectsFactory.objectByName(plantKind);
		plant.setKind(plantKind);
		plant.setPos(pos);
		return plant;
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public int price() {
		return 10 * quantity();
	}

	@Override
	public String desc() {
		return kindDesc;
	}

	@Override
	public String info() {
		return Utils.format(R.string.Plant_Info, Utils.indefinite(plantName), desc());
	}

	@Override
	public String bag() {
		return "SeedPouch";
	}
}
