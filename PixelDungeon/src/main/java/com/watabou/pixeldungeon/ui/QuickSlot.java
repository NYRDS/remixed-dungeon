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
package com.watabou.pixeldungeon.ui;

import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Button;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.wands.Wand;
import com.watabou.pixeldungeon.items.weapon.Weapon;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.windows.WndBag;

import java.util.ArrayList;
import java.util.HashMap;

public class QuickSlot extends Button implements WndBag.Listener {

	private static final String TXT_SELECT_ITEM = Game.getVar(R.string.QuickSlot_SelectedItem);

	private static ArrayList<QuickSlot> slots = new ArrayList<>();
	private static HashMap<Integer, Object> qsStorage = new HashMap<>();

	// Either Item or Class<? extends Item>
	private Object quickslotItem;

	private Item itemInSlot;
	private ItemSlot slot;

	private Image crossB;
	private Image crossM;

	private boolean targeting = false;
	private Item lastItem = null;
	private Char lastTarget = null;

	private int index;

	public QuickSlot() {
		super();
		slots.add(this);

		index = slots.size() - 1;
		if (qsStorage.containsKey(index)) {
			selectItem(qsStorage.get(index), index);
		} else {
			item(select());
		}
	}

	@Override
	public void destroy() {
		super.destroy();
		slots.clear();
		lastItem = null;
		lastTarget = null;
	}

	@Override
	protected void createChildren() {
		super.createChildren();

		slot = new ItemSlot() {
			@Override
			protected void onClick() {
				if (targeting) {
					GameScene.handleCell(lastTarget.getPos());
				} else {
					Item item = select();
					if (item == lastItem) {
						useTargeting();
					} else {
						lastItem = item;
					}
					if (item != null) {
						item.execute(Dungeon.hero);
					}
				}
			}

			@Override
			protected boolean onLongClick() {
				return QuickSlot.this.onLongClick();
			}

			@Override
			protected void onTouchDown() {
				icon.lightness(0.7f);
			}

			@Override
			protected void onTouchUp() {
				icon.resetColor();
			}
		};
		add(slot);

		crossB = Icons.TARGET.get();
		crossB.setVisible(false);
		add(crossB);

		crossM = new Image();
		crossM.copy(crossB);
	}

	@Override
	protected void layout() {
		super.layout();

		slot.fill(this);

		crossB.x = PixelScene.align(x + (width - crossB.width) / 2);
		crossB.y = PixelScene.align(y + (height - crossB.height) / 2);
	}

	@Override
	protected void onClick() {
		GameScene.selectItem(this, WndBag.Mode.QUICKSLOT, TXT_SELECT_ITEM);
	}

	@Override
	protected boolean onLongClick() {
		GameScene.selectItem(this, WndBag.Mode.QUICKSLOT, TXT_SELECT_ITEM);
		return true;
	}

	@SuppressWarnings("unchecked")
	private Item select() {
		Object quickslotItem = quickslotItem();
		
		if (quickslotItem instanceof Item) {
			return (Item) quickslotItem;
		}

		if (quickslotItem != null) {
			Item item = Dungeon.hero.belongings.getItem((Class<? extends Item>) quickslotItem);
			return item != null ? item : Item.virtual((Class<? extends Item>) quickslotItem);
		}

		return null;
	}

	@Override
	public void onSelect(Item item) {
		if (item != null) {
			quickslotItem(item.stackable ? item.getClass() : item);
			refresh();
		}
	}

	public void item(Item item) {
		slot.item(item);
		itemInSlot = item;
		enableSlot();
	}

	public void enable(boolean value) {
		active = value;
		if (value) {
			enableSlot();
		} else {
			slot.enable(false);
		}
	}

	private void enableSlot() {
		slot.enable(itemInSlot != null && itemInSlot.quantity() > 0
				&& (Dungeon.hero.belongings.backpack.contains(itemInSlot) || itemInSlot.isEquipped(Dungeon.hero)));
	}

	private void useTargeting() {

		updateTargetingState();

		if(!targeting) {
			if(lastItem instanceof Wand || lastItem instanceof Weapon) {
				lastTarget = Dungeon.hero.getNearestEnemy();
				updateTargetingState();
			}
		}

		if (targeting) {
			if (Actor.all().contains(lastTarget)) {
				lastTarget.getSprite().getParent().add(crossM);
				crossM.point(DungeonTilemap.tileToWorld(lastTarget.getPos()));
				crossB.setVisible(true);
			} else {
				lastTarget = null;
			}
		}
	}

	private void updateTargetingState() {
		targeting = lastTarget != null && lastTarget.isAlive() && Dungeon.visible[lastTarget.getPos()];
	}

	public static void refresh() {
		Game.executeInGlThread(new Runnable() {
			@Override
			public void run() {
				for (QuickSlot slot : slots) {
					slot.item(slot.select());
				}
			}
		});
	}

	public static void target(Item item, Char target) {
		for (QuickSlot slot : slots) {
			if (item == slot.lastItem && target != Dungeon.hero) {
				slot.lastTarget = target;
				HealthIndicator.instance.target(target);
			}
		}
	}

	public static void cancel() {
		for (QuickSlot slot : slots) {
			if (slot != null && slot.targeting) {
				slot.crossB.setVisible(false);
				slot.crossM.remove();
				slot.targeting = false;
			}
		}
	}

	public static Object getEarlyLoadItem(int n) {
		if (qsStorage.containsKey(n)) {
			return qsStorage.get(n);
		}
		return null;
	}

	public static Object getItem(int n) {
		if (n < slots.size()) {
			return slots.get(n).select();
		}
		return null;
	}

	public static void selectItem(Object object, int n) {
		if (n < slots.size()) {
			QuickSlot slot = slots.get(n);
			slot.quickslotItem(object);
			slot.onSelect(slot.select());
		} else {
			qsStorage.put(n, object);
		}

	}

	public static void cleanStorage() {
		qsStorage.clear();
	}

	private Object quickslotItem() {
		return quickslotItem;
	}

	private void quickslotItem(Object quickslotItem) {
		qsStorage.put(index, quickslotItem);
		this.quickslotItem = quickslotItem;
	}
}
