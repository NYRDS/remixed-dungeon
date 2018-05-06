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

import com.nyrds.pixeldungeon.mechanics.spells.SpellHelper;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.windows.HBox;
import com.nyrds.pixeldungeon.windows.VBox;
import com.nyrds.pixeldungeon.windows.WndHeroSpells;
import com.watabou.noosa.Game;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.Chrome;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.plants.Plant;
import com.watabou.pixeldungeon.scenes.CellSelector;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.windows.WndHero;
import com.watabou.pixeldungeon.windows.WndInfoCell;
import com.watabou.pixeldungeon.windows.WndInfoItem;
import com.watabou.pixeldungeon.windows.WndInfoMob;
import com.watabou.pixeldungeon.windows.WndInfoPlant;
import com.watabou.pixeldungeon.windows.WndMessage;
import com.watabou.pixeldungeon.windows.WndTradeItem;
import com.watabou.pixeldungeon.windows.elements.Tool;

import java.util.ArrayList;

public class Toolbar extends Component {

	private Tool btnSpells;
	private Tool btnWait;
	private Tool btnSearch;
	private Tool btnInfo;
	private InventoryTool btnInventory;

	private VBox toolbar   = new VBox();
	private HBox slotBox   = new HBox();
	private HBox actionBox = new HBox();

	public static final int MAX_SLOTS = 25;


	private ArrayList<QuickslotTool> slots = new ArrayList<>();


	private boolean lastEnabled = true;

	public Toolbar(final Hero hero) {
		super();

		actionBox.add(btnWait = new Tool(10, Chrome.Type.ACTION_BUTTON) {
			@Override
			protected void onClick() {
				hero.rest(false);
			}

			protected boolean onLongClick() {
				Dungeon.hero.rest(true);
				return true;
			}
		});

		actionBox.add(btnSearch = new Tool(11,Chrome.Type.ACTION_BUTTON) {
			@Override
			protected void onClick() {
				Dungeon.hero.search(true);
			}
		});

		actionBox.add(btnInfo = new Tool(12,Chrome.Type.ACTION_BUTTON) {
			@Override
			protected void onClick() {
				GameScene.selectCell(informer);
			}
		});

		if (hero.spellUser) {
			actionBox.add(btnSpells = new Tool(SpellHelper.iconIdByHero(hero),Chrome.Type.ACTION_BUTTON) {
			@Override
			protected void onClick() {
				GameScene.show(new WndHeroSpells(null));
			}
		});
		}
		actionBox.add(btnInventory = new InventoryTool());

		for(int i = 0;i<MAX_SLOTS;i++) {
			slots.add(new QuickslotTool());
		}

		width = Game.width();

		height = Math.max(btnInfo.height(), slots.get(0).height());
	}

	@Override
	protected void layout() {

		toolbar.remove(slotBox);
		toolbar.remove(actionBox);

		slotBox.setAlign(HBox.Align.Center);

		for (QuickslotTool tool:slots) {
			slotBox.remove(tool);
		}

		final int active_slots = PixelDungeon.quickSlots();

		if(active_slots > 0) {
			for (int i = 0; i < active_slots; i++) {
				slots.get(i).show(true);
				slotBox.add(slots.get(i));
			}

			slotBox.setSize(width(), slots.get(0).height());

			if (!slotBox.willFit()) {
				PixelDungeon.quickSlots(active_slots - 1);
				return;
			}

			toolbar.add(slotBox);
		}


		actionBox.setAlign(HBox.Align.Right);
		actionBox.setSize(width(),btnInfo.height());
		toolbar.add(actionBox);

		toolbar.setAlign(VBox.Align.Bottom);
		toolbar.setRect(x,y,width,height);
		add(toolbar);
	}

	@Override
	public void update() {
		super.update();

		if (lastEnabled != Dungeon.hero.isReady()) {
			lastEnabled = Dungeon.hero.isReady();

			for (Gizmo tool : members) {
				if (tool instanceof Tool) {
					((Tool) tool).enable(lastEnabled);
				}
			}
		}
	}

	private static CellSelector.Listener informer = new CellSelector.Listener() {
		@Override
		public void onSelect(Integer cell) {

			if (cell == null) {
				return;
			}

			if (cell < 0
					|| cell > Dungeon.level.getLength()
					|| (!Dungeon.level.visited[cell] && !Dungeon.level.mapped[cell])) {
				GameScene.show(new WndMessage(Game
						.getVar(R.string.Toolbar_Info1)));
				return;
			}

			if (!Dungeon.visible[cell]) {
				GameScene.show(new WndInfoCell(cell));
				return;
			}

			if (cell == Dungeon.hero.getPos()) {
				GameScene.show(new WndHero());
				return;
			}

			Mob mob = (Mob) Actor.findChar(cell);
			if (mob != null) {
				GameScene.show(new WndInfoMob(mob));
				return;
			}

			Heap heap = Dungeon.level.getHeap(cell);
			if (heap != null) {
				if (heap.type == Heap.Type.FOR_SALE && heap.size() == 1
						&& heap.peek().price() > 0) {
					GameScene.show(new WndTradeItem(heap, false));
				} else {
					GameScene.show(new WndInfoItem(heap));
				}
				return;
			}

			Plant plant = Dungeon.level.plants.get(cell);
			if (plant != null) {
				GameScene.show(new WndInfoPlant(plant));
				return;
			}

			GameScene.show(new WndInfoCell(cell));
		}

		@Override
		public String prompt() {
			return Game.getVar(R.string.Toolbar_Info2);
		}
	};

	public void pickup(Item item) {
		btnInventory.pickUp(item);
	}

	private static class QuickslotTool extends Tool {

		private QuickSlot slot;

		QuickslotTool() {
			super(-1, Chrome.Type.QUICKSLOT);

			slot = new QuickSlot();
			add(slot);
		}

		@Override
		protected void layout() {
			super.layout();
			slot.setRect(base.x,base.y,base.width(),base.height());
		}

		public void show(boolean value){
			setVisible(value);
			enable(value);
		}
		
		@Override
		public void enable(boolean value) {
			slot.enable(value);
			active = value;
		}
	}

	@Override
	public float top() {
		int qslots = PixelDungeon.quickSlots();
		if(qslots==0) {
			return btnInfo.top();
		} else {
			return Math.min(btnInfo.top(), slots.get(qslots - 1).top());
		}
	}
}
