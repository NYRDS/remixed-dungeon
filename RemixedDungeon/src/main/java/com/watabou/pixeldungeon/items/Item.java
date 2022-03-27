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
import com.nyrds.lua.LuaUtils;
import com.nyrds.pixeldungeon.items.ItemOwner;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.items.common.Library;
import com.nyrds.pixeldungeon.levels.objects.Presser;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKindWithId;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.ml.actions.UseItem;
import com.nyrds.pixeldungeon.utils.CharsList;
import com.nyrds.pixeldungeon.utils.EntityIdSource;
import com.nyrds.pixeldungeon.utils.ItemsList;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.Scrambler;
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
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.Glowing;
import com.watabou.pixeldungeon.sprites.MissileSprite;
import com.watabou.pixeldungeon.ui.QuickSlot;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.LuaTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import clone.org.json.JSONException;
import clone.org.json.JSONObject;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import lombok.var;

public class Item extends Actor implements Bundlable, Presser, NamedEntityKindWithId {

	private static final String TXT_TO_STRING = "%s";
	private static final String TXT_TO_STRING_X = "%s x%d";
	private static final String TXT_TO_STRING_LVL = "%s%+d";
	private static final String TXT_TO_STRING_LVL_X = "%s%+d x%d";


	private static final float TIME_TO_THROW = 1.0f;
	protected static final float TIME_TO_PICK_UP = 1.0f;
	private static final float TIME_TO_DROP = 0.5f;

	protected static final String AC_DROP = "Item_ACDrop";
	protected static final String AC_THROW = "Item_ACThrow";

	@NotNull
	private String defaultAction = AC_THROW;

	@Packable(defaultValue = "-1")//EntityIdSource.INVALID_ID
	private int id = EntityIdSource.INVALID_ID;

	@NotNull
	protected String name = getClassParam("Name", StringsManager.getVar(R.string.Item_Name), false);
    @NotNull
	protected String info = getClassParam("Info", StringsManager.getVar(R.string.Item_Info), false);
    @NotNull
	protected String info2 = getClassParam("Info2", StringsManager.getVar(R.string.Item_Info2), false);

    protected int image = 0;
	protected int overlayIndex = -1;

	static private final String overlayFile = "items/overlays.png";
	protected String imageFile;

	public boolean stackable = false;

	private int quantity = Scrambler.scramble(1);

	private int level = Scrambler.scramble(0);

	@Packable
	@Getter
	@Setter
	private boolean levelKnown = false;

	@Packable
	@Getter
	@Setter
	public boolean cursed;

	@Packable
	@Getter
	@Setter
	private boolean cursedKnown;

	@Setter
	@Getter
	@Packable(defaultValue = "-1")
	private int quickSlotIndex = -1;


	private static final Comparator<Item> itemComparator = (lhs, rhs) -> {

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

	@LuaInterface
	public LuaTable actions_l(Char hero) {
		return LuaUtils.CollectionToTable(actions(hero));
	}

    public ArrayList<String> actions(Char hero) {
		ArrayList<String> actions = new ArrayList<>();
		if(!isEquipped(hero)) {
			actions.add(AC_DROP);
		}
		actions.add(AC_THROW);
		return actions;
	}

	public boolean doPickUp(@NotNull Char hero) {
		if (collect(hero.getBelongings().backpack)) {
			GameScene.pickUp(this);
			Sample.INSTANCE.play(Assets.SND_ITEM);
			hero.spend(TIME_TO_PICK_UP);
			return true;
		} else {
			return false;
		}
	}

	public void doDrop(@NotNull Char chr) {
		chr.spend(TIME_TO_DROP);
		int pos = chr.getPos();
		chr.level().animatedDrop(detachAll(chr.getBelongings().backpack), pos);
	}

	public void doThrow(@NotNull Char chr) {
		chr.selectCell(new Thrower(this));
	}

	public void execute(@NotNull Char chr, @NotNull String action) {
		GLog.debug("%s: %s by %s", getEntityKind(), action, chr.getEntityKind());
		chr.getBelongings().setSelectedItem(this);
		_execute(chr, action);
		chr.getBelongings().setSelectedItem(ItemsList.DUMMY);
	}

	protected void _execute(@NotNull Char chr, @NotNull String action) {
		if (action.equals(AC_DROP)) {
			doDrop(chr);
		} else if (action.equals(AC_THROW)) {
			doThrow(chr);
		}
	}

	public void execute(@NotNull Char hero) {

		if(hero.getHeroClass().forbidden(getDefaultAction())){
			setDefaultAction(AC_THROW);
		}

		String defaultAction = getDefaultAction();

		if(actions(hero).contains(defaultAction)) {
			hero.nextAction(new UseItem(this, defaultAction));
		}
	}

	protected void onThrow(int cell, @NotNull Char thrower) {
		dropTo(cell, thrower);
	}

	@LuaInterface
	public void dropTo(int cell) {
		dropTo(cell, getOwner());
	}

	public void dropTo(int cell, @NotNull Char thrower) {
		if(quickSlotIndex!=-1) {
			QuickSlot.refresh(thrower);
		}

		thrower.level().animatedDrop(this, cell);
	}

	public boolean collect(@NotNull Bag container) {
		setOwner(container.getOwner());

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
			for (Item item : items) {
				if (item.getEntityKind().equals(getEntityKind()) && item.level() == level()) {
					item.quantity(item.quantity() + quantity());
					QuickSlot.refresh(getOwner());
					return true;
				}
			}
		}

		if (items.size() < (container instanceof Backpack ? container.getSize() + 1 : container.getSize())) {
			items.add(this);
			Collections.sort(items, itemComparator);

			if (owner == Dungeon.hero && owner.isAlive()) {
				Badges.validateItemLevelAcquired(this);
				QuickSlot.refresh(getOwner());
			}

			return true;
		}

		if (owner == Dungeon.hero && owner.isAlive()) {
            GLog.n(StringsManager.getVar(R.string.Item_PackFull), name());
		}

		setOwner(CharsList.DUMMY);

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
				if(container.getOwner() instanceof Hero) {
					QuickSlot.refresh(getOwner());
				}

				Item detached = ItemFactory.itemByName(getEntityKind());
				detached.quantity(n);
				detached.level(level());
				detached.onDetach();
				return detached;
			}
		}
	}

	public final Item detachAll(@NotNull Bag container) {

    	if (container.contains(this)) {
    		container.remove(this);
			onDetach();
		}

		QuickSlot.refresh(container.getOwner());
    	return this;
	}

	protected void onDetach() {
    	setOwner(CharsList.DUMMY);
	}

	public Item upgrade() {

		setCursed(false);
		setCursedKnown(true);
		this.level(this.level() + 1);

		QuickSlot.refresh(getOwner());

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

		QuickSlot.refresh(owner);

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
		return isLevelKnown() && isCursedKnown();
	}

	public boolean isEquipped(@NotNull Char chr) {
    	return chr.getBelongings().isEquipped(this);
	}

	public void removeItemFrom(@NotNull Char hero) {
		setCursed(false);
		if (isEquipped(hero)) {
			((EquipableItem) this).doUnequip(hero, false);
		}
		hero.getBelongings().removeItem(this);
		onDetach();

		QuickSlot.refresh(hero);
	}

	public Item identify() {

		setLevelKnown(true);
		setCursedKnown(true);

		Library.identify(Library.ITEM,getEntityKind());

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

	public Glowing glowing() {
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
			EventCollector.logException("negative quantity for:" + getEntityKind());
		}

		quantity = Scrambler.scramble(value);
		return this;
	}

	public int price() {
		return 0;
	}

	protected int adjustPrice(int price) {
		if (isCursed() && isCursedKnown()) {
			price /= 2;
		}
		if (isLevelKnown()) {
			if (level() > 0) {
				price *= (level() + 1);
			} else if (level() < 0) {
				price /= (1 - level());
			}
		}

		if (price < 1) {
			price = 1;
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
	protected boolean act() {
		deactivateActor();
		return true;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		bundle.put(QUANTITY, quantity());
		bundle.put(LEVEL, level());
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {

		getId();

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

	@LuaInterface
	public void cast(final @NotNull Char user, int dst) {

	    if(quantity()<=0) {
	        return;
        }

	    int pos = user.getPos();

		int cell = Ballistica.cast(pos, dst, false, true);

		Level level = user.level();
		if(level.distance(cell, dst) == 1) {
			val lo = level.getTopLevelObject(dst);
			if ( lo != null && lo.affectItems()) {
				cell = dst;
			}
		}

		user.getSprite().zap(cell);

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

		var sprite = ((MissileSprite) user.
				getSprite().
				getParent().
				recycle(MissileSprite.class));

		int finalCell = cell;
		sprite.reset(pos, cell, this, () -> {
					user.spend(finalDelay);
					item.onThrow(finalCell, user);
				});
	}

	@NotNull
	@LuaInterface
	@Getter
	private Char owner = CharsList.DUMMY;

	protected String getClassParam(String paramName, String defaultValue, boolean warnIfAbsent) {
		return Utils.getClassParam(getEntityKind(), paramName, defaultValue, warnIfAbsent);
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

	public Item quickSlotContent() {
		if(!stackable) {
			return this;
		}

		if(quantity() > 0) {
			return this;
		}

		return ItemFactory.virtual(this);
	}

	public boolean usableByHero() {
		return quantity() >= 1 && (Dungeon.hero.getItem(getEntityKind()).valid() || isEquipped(Dungeon.hero));
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
		if(getOwner().getHeroClass().forbidden(newDefaultAction)) {
			newDefaultAction = AC_THROW;
		}

		this.defaultAction = newDefaultAction;
	}

	@LuaInterface
	@Override
	public String getEntityKind() {
		return ItemFactory.itemNameByClass(getClass());
	}

	public int overlayIndex() {
		return overlayIndex;
	}

	public int getId() {
		if(id==EntityIdSource.INVALID_ID) {
			id = EntityIdSource.getNextId();
			ItemsList.add(this,id);
		}
		return id;
	}

	public String bag() {
		return Utils.EMPTY_STRING;
	}

	public float heapScale() {
		return 1.f;
	}

	public void charAct() {
	}

	public boolean valid() {
		return true;
	}

	//former IChaosItem
	public void ownerTakesDamage(int damage) {
	}

	public void ownerDoesDamage(int damage) {
	}

	public void setOwner(Char owner) {
		add(this);
		this.owner = owner;
	}

	public boolean selectedForAction() {
		return getOwner().getBelongings().getSelectedItem() == this;
	}

	@LuaInterface
	@Deprecated
	public String getClassName() { //for old mods compatibility
		return getEntityKind();
	}

	public void setImage(int image) {
		this.image = image;
	}
}
