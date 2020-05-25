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

import com.nyrds.LuaInterface;
import com.nyrds.Packable;
import com.nyrds.android.util.Scrambler;
import com.nyrds.pixeldungeon.items.ItemOwner;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.items.common.Library;
import com.nyrds.pixeldungeon.levels.objects.Presser;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.CharsList;
import com.nyrds.pixeldungeon.utils.EntityIdSource;
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
import com.watabou.pixeldungeon.actors.hero.Hero;
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

import lombok.SneakyThrows;

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

	@Packable(defaultValue = "-1")//EntityIdSource.INVALID_ID
	private int id = EntityIdSource.INVALID_ID;

	@NotNull
	protected String name = getClassParam("Name", Game.getVar(R.string.Item_Name), false);
	@NotNull
	protected String info = getClassParam("Info", Game.getVar(R.string.Item_Info), false);
	@NotNull
	protected String info2 = getClassParam("Info2", Game.getVar(R.string.Item_Info2), false);

	protected int image = 0;
	protected int overlayIndex = -1;

	static private final String overlayFile = "items/overlays.png";
	protected String imageFile;

	public  boolean stackable = false;

	private int     quantity  = Scrambler.scramble(1);

	private int     level      = Scrambler.scramble(0);

	@Packable
	private boolean levelKnown = false;

	@Packable
	public boolean cursed;

	@Packable
	public boolean cursedKnown;

	@Packable(defaultValue = "-1")
	private int quickSlotIndex = -1;

	private static Comparator<Item> itemComparator = (lhs, rhs) -> {

		if(lhs.isIdentified() &&  !rhs.isIdentified()) {
			return -1;
		}

		if(!lhs.isIdentified() && rhs.isIdentified()) {
			return 1;
		}

		if(!lhs.isIdentified() && !rhs.isIdentified()) {
			return 0;
		}

		return lhs.price() - rhs.price();
	};

    public ArrayList<String> actions(Char hero) {
		ArrayList<String> actions = new ArrayList<>();
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

	public void doDrop(Char chr) {
		chr.spendAndNext(TIME_TO_DROP);
		int pos = chr.getPos();
		Dungeon.level.animatedDrop(detachAll(chr.getBelongings().backpack), pos);
	}

	public void doThrow(Char chr) {
		chr.selectCell(thrower);
	}

	public void execute(Char chr, String action) {
		chr.getBelongings().setSelectedItem(this);

		if (action.equals(AC_DROP)) {
			doDrop(chr);
		} else if (action.equals(AC_THROW)) {
			doThrow(chr);
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

	protected void onThrow(int cell, Char thrower) {
		dropTo(cell);
	}

	public void dropTo(int cell) {
		if(quickSlotIndex!=-1) {
			QuickSlot.refresh();
		}

		Dungeon.level.animatedDrop(this, cell);
	}

	public boolean collect(Bag container) {

		ArrayList<Item> items = container.items;

		setOwner(container.owner);

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

		if (items.size() < (container instanceof Backpack ? container.getSize() + 1 : container.getSize())) {
			if (Dungeon.hero != null && Dungeon.hero.isAlive()) {
				Badges.validateItemLevelAcquired(this);
			}

			items.add(this);
			QuickSlot.refresh();
			Collections.sort(items, itemComparator);
			return true;
		}

		setOwner(CharsList.DUMMY);
		GLog.n(Game.getVar(R.string.Item_PackFull), name());
		return false;
	}

	public boolean collect(@NotNull ItemOwner owner) {
		return owner.getBelongings().collect(this);
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

				Item detached = ItemFactory.itemByName(getClassName());
				detached.quantity(n);
				detached.level(level());
				detached.onDetach();
				return detached;
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

		setCursed(false);
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
		return isLevelKnown() ? level() : 0;
	}

	public boolean isUpgradable() {
		return true;
	}

	public boolean isIdentified() {
		return isLevelKnown() && cursedKnown;
	}

	public boolean isEquipped(@NotNull Char chr) {
    	return chr.getBelongings().isEquipped(this);
	}

	public void removeItemFrom(Char hero) {
		onDetach();
		setCursed(false);
		if (!(this instanceof EquipableItem) || !isEquipped(hero) || !((EquipableItem) this).doUnequip(hero, false)) {
			hero.getBelongings().removeItem(this);
		}

		QuickSlot.refresh();
	}

	public Item identify() {

		setLevelKnown(true);
		cursedKnown = true;

		Library.identify(Library.ITEM,getClassName());

		return this;
	}

    @NotNull
	@Override
	public String toString() {

		if (isLevelKnown() && level() != 0) {
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

	public Item quantity(int value) {

		if(value < 0) {
			EventCollector.logException();
		}

		quantity = Scrambler.scramble(value);
		return this;
	}

	public int price() {
		return 0;
	}

	protected int adjustPrice(int price) {
		if (isCursed() && cursedKnown) {
			price /= 2;
		}
		if (isLevelKnown()) {
			if (level() > 0) {
				price *= (level() + 1);
			} else if (level() < 0) {
				price /= (1 - level());
			}
		}

		return price;
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

		if(id==EntityIdSource.INVALID_ID) {
			id = EntityIdSource.getNextId();
		}

		quantity(bundle.getInt(QUANTITY));

		int level = bundle.getInt(LEVEL);
		if (level > 0) {
			upgrade(level);
		} else if (level < 0) {
			degrade(-level);
		}

		//We still need this because upgrade erase cursed flag
		setCursed(bundle.optBoolean("cursed",false));

		if(quickSlotIndex >= 0 ) {
			QuickSlot.selectItem(this, quickSlotIndex);
		}
	}

	public boolean dontPack() {
		return false;
	}

	public void cast(final Char user, int dst) {

	    if(quantity()<=0) {
	        return;
        }

	    int pos = user.getPos();

		final int cell = Ballistica.cast(pos, dst, false, true);
		user.getSprite().zap(cell);
		user.busy();

		Char enemy = Actor.findChar(cell);
		QuickSlot.target(this, enemy);

		float delay = TIME_TO_THROW;
		if (this instanceof MissileWeapon) {

			// FIXME
			delay *= ((MissileWeapon) this).attackDelayFactor(user);
			if (enemy != null && enemy.hasBuff(SnipersMark.class)) {
				delay *= 0.5f;
			}
		}
		final float finalDelay = delay;

		final Item item = detach(user.getBelongings().backpack);

		((MissileSprite) user.getSprite().getParent().recycle(MissileSprite.class)).
				reset(pos, cell, this, () -> {
					user.spendAndNext(finalDelay);
					item.onThrow(cell, user);
				});
	}

	@NotNull
	private Char owner = CharsList.DUMMY;

	private static   CellSelector.Listener thrower = new CellSelector.Listener() {
		@Override
		public void onSelect(Integer target, Char selector) {
			if (target != null) {
				selector.getBelongings().getSelectedItem().cast(selector, target);
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

	@SneakyThrows
	protected Item morphTo(@NotNull Class<? extends Item> itemClass) {
		Item result = itemClass.newInstance();
		result.quantity(quantity());
		return result;
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

	public String overlayFile() {
		return overlayFile;
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

	@Deprecated
	public Char getUser() {
		return getOwner();
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

		setCursed(itemDesc.optBoolean("cursed", false));

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
		return quantity() >= 1 && (Dungeon.hero.getBelongings().getItem(getClassName()) != null || isEquipped(Dungeon.hero));
	}

	public boolean announcePickUp() {
		return true;
	}

	@NotNull
	@LuaInterface
	public String getDefaultAction() {
		return defaultAction;
	}

	@LuaInterface
	public void setDefaultAction(@NotNull String newDefaultAction) {
		Char hero = getOwner();

		if(hero.getHeroClass().forbidden(newDefaultAction)) {
			newDefaultAction = AC_THROW;
		}

		this.defaultAction = newDefaultAction;
	}

	@Override
	public String getEntityKind() {
		return getClassName();
	}

	public int overlayIndex() {
		return overlayIndex;
	}

	public int getId() {
		if(id==EntityIdSource.INVALID_ID) {
			id = EntityIdSource.getNextId();
		}
		return id;
	}

	public boolean isLevelKnown() {
		return levelKnown;
	}

	public void setLevelKnown(boolean levelKnown) {
		this.levelKnown = levelKnown;
	}

	public String bag() {
		return Utils.EMPTY_STRING;
	}

	@LuaInterface
	@NotNull
	public Char getOwner() {
		return owner;
	}

	public void setOwner(@NotNull Char owner) {
		this.owner = owner;
	}

	public boolean isCursed() {
		return cursed;
	}

	public boolean setCursed(boolean cursed) {
		this.cursed = cursed;
		return this.cursed;
	}
}
