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
package com.watabou.pixeldungeon.items;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.nyrds.android.util.Scrambler;
import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.retrodungeon.items.common.ItemFactory;
import com.nyrds.retrodungeon.items.common.Library;
import com.nyrds.retrodungeon.levels.objects.Presser;
import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.SnipersMark;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.bags.Bag;
import com.watabou.pixeldungeon.items.weapon.missiles.MissileWeapon;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.scenes.CellSelector;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.sprites.MissileSprite;
import com.watabou.pixeldungeon.ui.QuickSlot;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Item implements Bundlable, Presser {

	private static final   String TXT_PACK_FULL = Game.getVar(R.string.Item_PackFull);
	protected static final String TXT_DIR_THROW = Game.getVar(R.string.Item_DirThrow);

	private static final String TXT_TO_STRING       = "%s";
	private static final String TXT_TO_STRING_X     = "%s x%d";
	private static final String TXT_TO_STRING_LVL   = "%s%+d";
	private static final String TXT_TO_STRING_LVL_X = "%s%+d x%d";

	protected static final float TIME_TO_THROW   = 1.0f;
	protected static final float TIME_TO_PICK_UP = 1.0f;
	protected static final float TIME_TO_DROP    = 0.5f;

	private static final   String AC_DROP  = Game.getVar(R.string.Item_ACDrop);
	protected static final String AC_THROW = Game.getVar(R.string.Item_ACThrow);

	@NonNull
	public String defaultAction = AC_THROW;

	protected String name = getClassParam("Name", Game.getVar(R.string.Item_Name), false);
	protected String info = getClassParam("Info", Game.getVar(R.string.Item_Info), false);
	protected String info2 = getClassParam("Info2", Game.getVar(R.string.Item_Info2), false);

	protected int image = 0;
	protected String imageFile;

	public  boolean stackable = false;
	private int     quantity  = Scrambler.scramble(1);

	private int     level      = Scrambler.scramble(0);
	public  boolean levelKnown = false;

	public boolean cursed;
	public boolean cursedKnown;

	private static Comparator<Item> itemComparator = new Comparator<Item>() {
		@Override
		public int compare(Item lhs, Item rhs) {
			return Generator.Category.order(lhs) - Generator.Category.order(rhs);
		}
	};

	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = new ArrayList<>();
		actions.add(AC_DROP);
		actions.add(AC_THROW);
		return actions;
	}

	public boolean doPickUp(Hero hero) {
		if (collect(hero.belongings.backpack)) {

			GameScene.pickUp(this);
			Sample.INSTANCE.play(Assets.SND_ITEM);
			hero.spendAndNext(TIME_TO_PICK_UP);
			return true;

		} else {
			return false;
		}
	}

	public void doDrop(Hero hero) {
		hero.spendAndNext(TIME_TO_DROP);
		Dungeon.level.drop(detachAll(hero.belongings.backpack), hero.getPos()).sprite.drop(hero.getPos());
	}

	public void doThrow(Hero hero) {
		GameScene.selectCell(thrower);
	}

	public void execute(Hero hero, String action) {

		setCurUser(hero);
		curItem = this;

		if (action.equals(AC_DROP)) {
			doDrop(hero);
		} else if (action.equals(AC_THROW)) {
			doThrow(hero);
		}
	}

	public void execute(Hero hero) {
		execute(hero, defaultAction);
	}

	protected void onThrow(int cell) {
		Heap heap = Dungeon.level.drop(this, cell);
		if (!heap.isEmpty()) {
			heap.sprite.drop(cell);
		}
	}

	public boolean collect(Bag container) {

		ArrayList<Item> items = container.items;

		if (items.contains(this)) {
			return true;
		}

		for (Item item : items) {
			if (item instanceof Bag && ((Bag) item).grab(this)) {
				return collect((Bag) item);
			}
		}

		if (stackable) {
			Class<?> c = getClass();
			for (Item item : items) {
				if (item.getClass() == c && item.level() == level()) {
					item.quantity(item.quantity() + quantity());
					item.updateQuickslot();
					return true;
				}
			}
		}

		if (items.size() < container.size) {

			if (Dungeon.hero != null && Dungeon.hero.isAlive()) {
				Badges.validateItemLevelAcquired(this);
			}

			items.add(this);
			QuickSlot.refresh();
			Collections.sort(items, itemComparator);
			return true;

		} else {

			GLog.n(TXT_PACK_FULL, name());
			return false;

		}
	}

	public boolean collect(Hero hero) {
		return collect(hero.belongings.backpack);
	}

	public final Item detach(Bag container) {
		if (quantity() <= 0) {
			return null;
		} else {
			if (quantity() == 1) {
				return detachAll(container);
			} else {
				quantity(quantity() - 1);
				updateQuickslot();
				try {
					Item detached = getClass().newInstance();
					detached.level(level());
					detached.onDetach();
					return detached;
				} catch (Exception e) {
					throw new TrackedRuntimeException(e);
				}
			}
		}
	}

	public final Item detachAll(Bag container) {

		for (Item item : container.items) {
			if (item == this) {
				container.items.remove(this);
				item.onDetach();
				QuickSlot.refresh();
				return this;
			} else if (item instanceof Bag) {
				Bag bag = (Bag) item;
				if (bag.contains(this)) {
					return detachAll(bag);
				}
			}
		}

		return this;
	}

	protected void onDetach() {
	}

	public Item upgrade() {

		cursed = false;
		cursedKnown = true;
		this.level(this.level() + 1);

		return this;
	}

	public Item upgrade(int n) {
		for (int i = 0; i < n; i++) {
			upgrade();
		}

		return this;
	}

	public Item degrade() {

		this.level(this.level() - 1);

		return this;
	}

	public Item degrade(int n) {
		for (int i = 0; i < n; i++) {
			degrade();
		}

		return this;
	}

	public int visiblyUpgraded() {
		return levelKnown ? level() : 0;
	}

	public boolean isUpgradable() {
		return true;
	}

	public boolean isIdentified() {
		return levelKnown && cursedKnown;
	}

	public boolean isEquipped(Hero hero) {
		return this.equals(hero.belongings.weapon) ||
				this.equals(hero.belongings.armor) ||
				this.equals(hero.belongings.ring1) ||
				this.equals(hero.belongings.ring2);
	}

	public void removeItemFrom(Hero hero) {
		onDetach();
		cursed = false;
		if (!(this instanceof EquipableItem) || !isEquipped(hero) || !((EquipableItem) this).doUnequip(hero, false)) {
			hero.belongings.removeItem(this);
		}

		updateQuickslot();
	}

	public Item identify() {

		levelKnown = true;
		cursedKnown = true;

		Library.identify(Library.ITEM,ItemFactory.itemNameByClass(getClass()));

		return this;
	}

	public static void evoke(Hero hero) {
		hero.getSprite().emitter().burst(Speck.factory(Speck.EVOKE), 5);
	}

	@Override
	public String toString() {

		if (levelKnown && level() != 0) {
			if (quantity() > 1) {
				return Utils.format(TXT_TO_STRING_LVL_X, name(), level(), quantity());
			} else {
				return Utils.format(TXT_TO_STRING_LVL, name(), level());
			}
		} else {
			if (quantity() > 1) {
				return Utils.format(TXT_TO_STRING_X, name(), quantity());
			} else {
				return Utils.format(TXT_TO_STRING, name());
			}
		}
	}

	public String name() {
		return name;
	}

	public final String trueName() {
		return name;
	}

	public ItemSprite.Glowing glowing() {
		return null;
	}

	public String info() {
		return desc();
	}

	public String desc() {
		return info;
	}

	public int quantity() {
		return Scrambler.descramble(quantity);
	}

	public void quantity(int value) {
		quantity = Scrambler.scramble(value);
	}

	public int price() {
		return 0;
	}

	public static Item virtual(Class<? extends Item> cl) {
		try {
			Item item = cl.newInstance();
			item.quantity(0);
			return item;
		} catch (Exception e) {
			throw new TrackedRuntimeException("Item.virtual");
		}
	}

	public Item random() {
		return this;
	}

	public String status() {
		return quantity() != 1 ? Integer.toString(quantity()) : null;
	}

	public void updateQuickslot() {
		QuickSlot.refresh();
	}

	private static final String QUANTITY     = "quantity";
	private static final String LEVEL        = "level";
	private static final String LEVEL_KNOWN  = "levelKnown";
	private static final String CURSED       = "cursed";
	private static final String CURSED_KNOWN = "cursedKnown";
	private static final String QUICKSLOT    = "quickslot";
	private static final String QUICKSLOT_2  = "quickslot_2";
	private static final String QUICKSLOT_3  = "quickslot_3";

	@Override
	public void storeInBundle(Bundle bundle) {
		bundle.put(QUANTITY, quantity());
		bundle.put(LEVEL, level());
		bundle.put(LEVEL_KNOWN, levelKnown);
		bundle.put(CURSED, cursed);
		bundle.put(CURSED_KNOWN, cursedKnown);

		if (this == QuickSlot.getItem(0)) {
			bundle.put(QUICKSLOT, true);
		}

		if (this == QuickSlot.getItem(1)) {
			bundle.put(QUICKSLOT_2, true);
		}

		if (this == QuickSlot.getItem(2)) {
			bundle.put(QUICKSLOT_3, true);
		}
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		quantity(bundle.getInt(QUANTITY));
		levelKnown = bundle.getBoolean(LEVEL_KNOWN);
		cursedKnown = bundle.getBoolean(CURSED_KNOWN);

		int level = bundle.getInt(LEVEL);
		if (level > 0) {
			upgrade(level);
		} else if (level < 0) {
			degrade(-level);
		}

		cursed = bundle.getBoolean(CURSED);

		if (bundle.getBoolean(QUICKSLOT)) {
			QuickSlot.selectItem(this, 0);
		}

		if (bundle.getBoolean(QUICKSLOT_2)) {
			QuickSlot.selectItem(this, 1);
		}

		if (bundle.getBoolean(QUICKSLOT_3)) {
			QuickSlot.selectItem(this, 2);
		}
	}

	public boolean dontPack() {
		return false;
	}

	public void cast(final Hero user, int dst) {

		setCurUser(user);

		final int cell = Ballistica.cast(user.getPos(), dst, false, true);
		user.getSprite().zap(cell);
		user.busy();

		Char enemy = Actor.findChar(cell);
		QuickSlot.target(this, enemy);

		float delay = TIME_TO_THROW;
		if (this instanceof MissileWeapon) {

			// FIXME
			delay *= ((MissileWeapon) this).speedFactor(user);
			if (enemy != null && enemy.buff(SnipersMark.class) != null) {
				delay *= 0.5f;
			}
		}
		final float finalDelay = delay;

		final Item item = detach(user.belongings.backpack);

		((MissileSprite) user.getSprite().getParent().recycle(MissileSprite.class)).
				reset(user.getPos(), cell, this, new Callback() {
					@Override
					public void call() {
						item.onThrow(cell);
						user.spendAndNext(finalDelay);
					}
				});
	}

	private static   Hero                  curUser = null;
	protected static Item                  curItem = null;
	protected static CellSelector.Listener thrower = new CellSelector.Listener() {
		@Override
		public void onSelect(Integer target) {
			if (target != null) {
				curItem.cast(getCurUser(), target);
			}
		}

		@Override
		public String prompt() {
			return TXT_DIR_THROW;
		}
	};

	protected String getClassParam(String paramName, String defaultValue, boolean warnIfAbsent) {
		return Utils.getClassParam(this.getClass().getSimpleName(), paramName, defaultValue, warnIfAbsent);
	}

	protected Item morphTo(Class<? extends Item> itemClass) {
		try {
			Item result = itemClass.newInstance();
			result.quantity(quantity());
			return result;
		} catch (Exception e) {
			throw new TrackedRuntimeException(e);
		}
	}

	public Item burn(int cell) {
		return this;
	}

	public Item freeze(int cell) {
		return this;
	}

	public Item poison(int pos) {
		return this;
	}

	public int level() {
		return Scrambler.descramble(level);
	}

	public void level(int level) {
		this.level = Scrambler.scramble(level);
	}

	public Item pick(Char ch, int pos) {
		return this;
	}

	public String imageFile() {
		String customImageFile = ItemSpritesDescription.getImageFile(this);

		if (customImageFile != null) {
			return customImageFile;
		}

		if (imageFile != null) {
			return imageFile;
		}
		return Assets.ITEMS;
	}

	public int image() {
		Integer customImageIndex = ItemSpritesDescription.getImageIndex(this);

		if (customImageIndex != null) {
			return customImageIndex;
		}

		return image;
	}

	public boolean isFliesStraight() {
		return ItemSpritesDescription.isFliesStraight(this);
	}

	public boolean isFliesFastRotating() {
		return ItemSpritesDescription.isFliesFastRotating(this);
	}

	protected static Hero getCurUser() {
		return curUser;
	}

	protected static void setCurUser(Hero curUser) {
		Item.curUser = curUser;
	}

	public void fromJson(JSONObject itemDesc) throws JSONException {
		quantity(Math.max(itemDesc.optInt("quantity",1),1));

		int level = itemDesc.optInt("level",0);


		if(level>0) {
			upgrade(level);
		}

		if(level<0) {
			degrade(-level);
		}

		cursed = itemDesc.optBoolean("cursed", false);

		if(itemDesc.optBoolean("identified",false)) {
			identify();
		}
	}

	@Override
	public boolean affectLevelObjects() {
		return true;
	}

	@Nullable
	public Emitter.Factory emitter() {
		return null;
	}

	public float emitterInterval() {
		return 0;
	}
}
