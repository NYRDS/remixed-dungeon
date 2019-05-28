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

import com.nyrds.Packable;
import com.nyrds.android.util.Scrambler;
import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.pixeldungeon.items.ItemOwner;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.items.common.Library;
import com.nyrds.pixeldungeon.levels.objects.Presser;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.SnipersMark;
import com.watabou.pixeldungeon.actors.hero.Backpack;
import com.watabou.pixeldungeon.actors.hero.Belongings;
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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Item implements Bundlable, Presser, NamedEntityKind {

	private static final String TXT_TO_STRING       = "%s";
	private static final String TXT_TO_STRING_X     = "%s x%d";
	private static final String TXT_TO_STRING_LVL   = "%s%+d";
	private static final String TXT_TO_STRING_LVL_X = "%s%+d x%d";


	private static final   float TIME_TO_THROW   = 1.0f;
	protected static final float TIME_TO_PICK_UP = 1.0f;
	private static final   float TIME_TO_DROP    = 0.5f;

	private static final   String AC_DROP  = "Item_ACDrop";
	protected static final String AC_THROW = "Item_ACThrow";

	@NotNull
	private String defaultAction = AC_THROW;

	@NotNull
	protected String name = getClassParam("Name", Game.getVar(R.string.Item_Name), false);
	@NotNull
	protected String info = getClassParam("Info", Game.getVar(R.string.Item_Info), false);
	@NotNull
	protected String info2 = getClassParam("Info2", Game.getVar(R.string.Item_Info2), false);

	protected int image = 0;

	protected String imageFile;

	public  boolean stackable = false;
	private int     quantity  = Scrambler.scramble(1);

	private int     level      = Scrambler.scramble(0);

	@Packable
	public  boolean levelKnown = false;

	@Packable
	public boolean cursed;

	@Packable
	public boolean cursedKnown;

	@Packable(defaultValue = "-1")
	private int quickSlotIndex = -1;

	private static Comparator<Item> itemComparator = (lhs, rhs) -> Generator.Category.order(lhs) - Generator.Category.order(rhs);

	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = new ArrayList<>();
		setUser(hero);
		actions.add(AC_DROP);
		actions.add(AC_THROW);
		return actions;
	}

	public boolean doPickUp(Char hero) {
		if (collect(hero.getBelongings().backpack)) {

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
		setUser(hero);
		curItem = this;

		if (action.equals(AC_DROP)) {
			doDrop(hero);
		} else if (action.equals(AC_THROW)) {
			doThrow(hero);
		}
	}

	public void execute(Hero hero) {
		if(hero.getHeroClass().forbidden(getDefaultAction())){
			setDefaultAction(AC_THROW);
		}

		if(actions(hero).contains(getDefaultAction())) {
			execute(hero, getDefaultAction());
		}
	}

	protected void onThrow(int cell) {
		dropTo(cell);
	}

	public void dropTo(int cell) {
		if(quickSlotIndex!=-1) {
			QuickSlot.refresh();
		}

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
			String c = getClassName();
			for (Item item : items) {
				if (item.getClassName().equals(c) && item.level() == level()) {
					item.quantity(item.quantity() + quantity());
					QuickSlot.refresh();
					return true;
				}
			}
		}

		if (items.size() < (container instanceof Backpack ? container.size + 1 : container.size)) {
			if (Dungeon.hero != null && Dungeon.hero.isAlive()) {
				Badges.validateItemLevelAcquired(this);
			}

			items.add(this);
			QuickSlot.refresh();
			Collections.sort(items, itemComparator);
			return true;
		} else {
			GLog.n(Game.getVar(R.string.Item_PackFull), name());
			return false;
		}
	}

	public boolean collect(ItemOwner owner) {
		Belongings belongings = owner.getBelongings();
		if(belongings==null) {
			return false;
		}

		return belongings.collect(this);
	}

	@Nullable
	public final Item detach(Bag container) {
		return detach(container, 1);
	}

	@Nullable
	public final Item detach(Bag container, int n) {
		if (quantity() <= 0) {
			return null;
		} else {
			if (quantity() <= n) {
				return detachAll(container);
			} else {
				quantity(quantity() - n);
				if(container.owner instanceof Hero) {
					QuickSlot.refresh();
				}
				try {
					Item detached = ItemFactory.itemByName(getClassName());
					detached.quantity(n);
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
				if(container.owner instanceof Hero) {
					QuickSlot.refresh();
				}
				return this;
			} else if (item instanceof Bag) {
				Bag bag = (Bag) item;
				if (bag.contains(this)) {
					return detachAll(bag);
				}
			}
		}
		QuickSlot.refresh();
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

	public boolean isEquipped(Char chr) {

		if(chr instanceof Hero) {
			Hero hero = (Hero)chr;
			return this.equals(hero.belongings.weapon) ||
					this.equals(hero.belongings.armor) ||
					this.equals(hero.belongings.ring1) ||
					this.equals(hero.belongings.ring2);
		}
		return false;
	}

	public void removeItemFrom(Char hero) {
		onDetach();
		cursed = false;
		if (!(this instanceof EquipableItem) || !isEquipped(hero) || !((EquipableItem) this).doUnequip(hero, false)) {
			hero.getBelongings().removeItem(this);
		}

		QuickSlot.refresh();
	}

	public Item identify() {

		levelKnown = true;
		cursedKnown = true;

		Library.identify(Library.ITEM,getClassName());

		return this;
	}

	public static void evoke(Hero hero) {
		hero.getSprite().emitter().burst(Speck.factory(Speck.EVOKE), 5);
	}

	@NotNull
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

		if(value < 0) {
			EventCollector.logException();
		}

		quantity = Scrambler.scramble(value);
	}

	public int price() {
		return 0;
	}

	public Item random() {
		return this;
	}

	public String status() {
		return quantity() != 1 ? Integer.toString(quantity()) : Utils.EMPTY_STRING;
	}

	private static final String QUANTITY     = "quantity";
	private static final String LEVEL        = "level";

	@Override
	public void storeInBundle(Bundle bundle) {
		bundle.put(QUANTITY, quantity());
		bundle.put(LEVEL, level());
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		quantity(bundle.getInt(QUANTITY));

		int level = bundle.getInt(LEVEL);
		if (level > 0) {
			upgrade(level);
		} else if (level < 0) {
			degrade(-level);
		}

		if(quickSlotIndex >= 0 ) {
			QuickSlot.selectItem(this, quickSlotIndex);
		}
	}

	public boolean dontPack() {
		return false;
	}

	public void cast(final Hero user, int dst) {

	    if(quantity()<=0) {
	        return;
        }

		setUser(user);

		final int cell = Ballistica.cast(user.getPos(), dst, false, true);
		user.getSprite().zap(cell);
		user.busy();

		Char enemy = Actor.findChar(cell);
		QuickSlot.target(this, enemy);

		float delay = TIME_TO_THROW;
		if (this instanceof MissileWeapon) {

			// FIXME
			delay *= ((MissileWeapon) this).speedFactor(user);
			if (enemy != null && enemy.hasBuff(SnipersMark.class)) {
				delay *= 0.5f;
			}
		}
		final float finalDelay = delay;

		final Item item = detach(user.belongings.backpack);

		((MissileSprite) user.getSprite().getParent().recycle(MissileSprite.class)).
				reset(user.getPos(), cell, this, () -> {
					user.spendAndNext(finalDelay);
					item.onThrow(cell);
				});
	}

	private static   Hero user = null;
	protected static Item curItem = null;

	private static   CellSelector.Listener thrower = new CellSelector.Listener() {
		@Override
		public void onSelect(Integer target) {
			if (target != null) {
				curItem.cast(getUser(), target);
			}
		}

		@Override
		public String prompt() {
			return Game.getVar(R.string.Item_DirThrow);
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

	public static Hero getUser() {
		return user;
	}

	protected static void setUser(Hero user) {
		Item.user = user;
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

	public void setQuickSlotIndex(int quickSlotIndex) {
		this.quickSlotIndex = quickSlotIndex;
	}

	public int getQuickSlotIndex() {
		return quickSlotIndex;
	}

	public String getClassName() {
		return ItemFactory.itemNameByClass(getClass());
	}


	public Item quickSlotContent() {
		if(!stackable) {
			return this;
		}

		if(quantity() > 0) {
			return this;
		}

		return ItemFactory.virtual(getClassName());
	}

	public boolean usableByHero() {
		return quantity() >= 1 && (Dungeon.hero.belongings.getItem(getClassName()) != null || isEquipped(Dungeon.hero));
	}

	public boolean announcePickUp() {
		return true;
	}

	@NotNull
	public String getDefaultAction() {
		return defaultAction;
	}

	public void setDefaultAction(@NotNull String newDefaultAction) {
		@Nullable
		Hero hero = getUser();

		if(hero==null) {
			this.defaultAction = newDefaultAction;
			return;
		}

		if(hero.getHeroClass().forbidden(newDefaultAction)) {
			newDefaultAction = AC_THROW;
		}

		this.defaultAction = newDefaultAction;
	}

	@Override
	public String getEntityKind() {
		return getClassName();
	}
}
