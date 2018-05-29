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

import com.nyrds.pixeldungeon.mechanics.spells.Spell;
import com.nyrds.pixeldungeon.mechanics.spells.SpellFactory;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.windows.WndHeroSpells;
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
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.HashMap;

import static com.watabou.pixeldungeon.scenes.PixelScene.uiCamera;

public class QuickSlot extends Button implements WndBag.Listener, WndHeroSpells.Listener {

	private static final String TXT_SELECT_ITEM = Game.getVar(R.string.QuickSlot_SelectedItem);
	private static final String QUICKSLOT = "quickslot";

	private static ArrayList<QuickSlot> slots = new ArrayList<>();
	private static HashMap<Integer, Object> qsStorage = new HashMap<>();

	// Either Item or Class<? extends Item> or Spell
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

				if(objectForSlot!=null) {
					updateSlotBySelection();
					return;
				}

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

						if(!Dungeon.hero.isAlive()) {
							return;
						}

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
		slot.setInQuickSlot(true);
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


	private void updateSlotBySelection(){
		selectItem(objectForSlot, index);
		objectForSlot = null;
		Game.scene().remove(prompt);
	}

	@Override
	protected void onClick() {
		if(objectForSlot != null) {
			updateSlotBySelection();
			return;
		}

		GameScene.selectItem(this, WndBag.Mode.QUICKSLOT, TXT_SELECT_ITEM);
	}

	@Override
	protected boolean onLongClick() {
		if(Dungeon.hero.spellUser) {
			GameScene.selectSpell(this);
		} else {
			GameScene.selectItem(this, WndBag.Mode.QUICKSLOT, TXT_SELECT_ITEM);
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	private Item select() {
		Object quickslotItem = this.quickslotItem;
		
		if (quickslotItem instanceof Item) {
			return (Item) quickslotItem;
		}

		if(quickslotItem instanceof Spell) {
			Spell spell = (Spell)quickslotItem;
			return spell.itemForSlot();
		}

		if (quickslotItem != null) {
			Item item = Dungeon.hero.belongings.getItem((String) quickslotItem);
			return item != null ? item : Item.virtual((String) quickslotItem);
		}


		return null;
	}

	private void item(Item item) {
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

	    if(itemInSlot instanceof Spell.SpellItem) {
	        slot.enable(true);
	        return;
        }

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
		Game.pushUiTask(new Runnable() {
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

	public static void cleanStorage() {
		qsStorage.clear();
	}

	private void quickslotItem(Object quickslotItem) {
        if(this.quickslotItem instanceof Item) {
            ((Item)this.quickslotItem).setQuickSlotIndex(-1);
        }

		if(quickslotItem instanceof Item) {
			((Item)quickslotItem).setQuickSlotIndex(index);
		}

		qsStorage.put(index, quickslotItem);
		this.quickslotItem = quickslotItem;
	}

	public static void save(Bundle bundle){
		ArrayList<String> classes = new ArrayList<>();

		for (int  i=0;i<slots.size();i++) {
			Object stored = qsStorage.get(i);
			if(stored instanceof Class) {
				classes.add(((Class)stored).getCanonicalName());
			} else if (stored instanceof Spell.SpellItem) {
				classes.add(((Spell.SpellItem)stored).spell().getSpellClass());
			} else {
				classes.add("");
			}
		}
		bundle.put(QUICKSLOT,classes.toArray(new String[classes.size()]));
	}

	public static void restore(Bundle bundle){
		qsStorage.clear();
		String [] classes = bundle.getStringArray(QUICKSLOT);
		for(int i =0;i< classes.length;i++) {
			if(!classes[i].isEmpty()) {
				try {
					if(SpellFactory.hasSpellForName(classes[i])) {
						Spell spell = SpellFactory.getSpellByName(classes[i]);
						selectItem(spell.itemForSlot(), i);
						continue;
					}
					selectItem(Class.forName(classes[i]),i );
				} catch (ClassNotFoundException e) {
					EventCollector.logException(e);
				}
			}
		}
		refresh();
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

	@Override
	public void onSelect(Item item) {
		if (item != null) {
			quickslotItem(item.stackable ? item.getClassName() : item);
			refresh();
		}
	}

	@Override
	public void onSelect(Spell.SpellItem spell) {
		if(spell != null) {
			quickslotItem(spell);
			refresh();
		}
	}

	private static Object objectForSlot;
	private static Toast prompt;
	static public void selectSlotFor(Object item) {
		objectForSlot = item;
		prompt = new Toast("Select quick slot for it");
		prompt.camera = uiCamera;
		prompt.setPos((uiCamera.width - prompt.width()) / 2, uiCamera.height - 60);

		Game.scene().add(prompt);

	}
}
