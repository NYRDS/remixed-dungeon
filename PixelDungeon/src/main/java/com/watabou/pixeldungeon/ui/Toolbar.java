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

import com.watabou.noosa.Game;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.PixelDungeon;
import com.nyrds.retrodungeon.ml.R;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.plants.Plant;
import com.watabou.pixeldungeon.scenes.CellSelector;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.windows.WndCatalogus;
import com.watabou.pixeldungeon.windows.WndHero;
import com.watabou.pixeldungeon.windows.WndInfoCell;
import com.watabou.pixeldungeon.windows.WndInfoItem;
import com.watabou.pixeldungeon.windows.WndInfoMob;
import com.watabou.pixeldungeon.windows.WndInfoPlant;
import com.watabou.pixeldungeon.windows.WndBag;
import com.watabou.pixeldungeon.windows.WndMessage;
import com.watabou.pixeldungeon.windows.WndTradeItem;
import com.watabou.pixeldungeon.windows.elements.Tool;

public class Toolbar extends Component {

	private Tool btnWait;
	private Tool btnSearch;
	private Tool btnInfo;
	private Tool btnInventory;
	
	private QuickslotTool  btnQuick1;
	private QuickslotTool  btnQuick2;
	private QuickslotTool  btnQuick3;

	private PickedUpItem pickedUp;

	private boolean lastEnabled = true;

	public Toolbar() {
		super();

		height = btnInventory.height();
	}

	@Override
	protected void createChildren() {

		//if(!Dungeon.level.isSafe())
		{
			add(btnWait = new Tool(0, 7, 20, 24) {
				@Override
				protected void onClick() {
					Dungeon.hero.rest(false);
				}

				protected boolean onLongClick() {
					Dungeon.hero.rest(true);
					return true;
				}
			});

			add(btnSearch = new Tool(20, 7, 20, 24) {
				@Override
				protected void onClick() {
					Dungeon.hero.search(true);
				}
			});

			add(btnInfo = new Tool(40, 7, 21, 24) {
				@Override
				protected void onClick() {
					GameScene.selectCell(informer);
				}
			});
		}

		add(btnInventory = new Tool(82, 7, 23, 24) {
			private GoldIndicator gold;

			@Override
			protected void onClick() {
				GameScene.show(new WndBag(Dungeon.hero.belongings.backpack,
						null, WndBag.Mode.ALL, null));
			}

			protected boolean onLongClick() {
				GameScene.show(new WndCatalogus());
				return true;
			}

			@Override
			protected void createChildren() {
				super.createChildren();
				gold = new GoldIndicator();
				add(gold);
			}

			@Override
			protected void layout() {
				super.layout();
				gold.fill(this);
			}
		});

		btnQuick1 = new QuickslotTool(105, 7, 22, 24);
		btnQuick2 = new QuickslotTool(105, 7, 22, 24);
		btnQuick3 = new QuickslotTool(105, 7, 22, 24);

		add(pickedUp = new PickedUpItem());
		
		update();
	}

	@Override
	protected void layout() {
		//if(!Dungeon.level.isSafe())
		{
			btnWait.setPos(x, y);
			btnSearch.setPos(btnWait.right(), y);
			btnInfo.setPos(btnSearch.right(), y);
			//btnResume.setPos(x, y - 24);
		}

		remove(btnQuick1);
		remove(btnQuick2);
		remove(btnQuick3);
		
		
		btnQuick1.setPos(width - btnQuick1.width(), y);
		btnQuick1.show(true);
		add(btnQuick1);
		
		if (! PixelDungeon.landscape() && PixelDungeon.thirdQuickslot()) {
			PixelDungeon.thirdQuickslot(false);
			PixelDungeon.secondQuickslot(true);
		}
		
		if (PixelDungeon.thirdQuickslot()) {			
			btnQuick2.setPos(btnQuick1.left() - btnQuick2.width(), y);
			btnQuick3.setPos(btnQuick2.left() - btnQuick3.width(), y);
			
			add(btnQuick2);
			add(btnQuick3);
			btnQuick2.show(true);
			btnQuick3.show(true);
			
			btnInventory.setPos(btnQuick3.left() - btnInventory.width(), y);
		} else if (PixelDungeon.secondQuickslot()) {
			btnQuick2.setPos(btnQuick1.left() - btnQuick2.width(), y);
			add(btnQuick2);
			btnQuick2.show(true);
			
			btnInventory.setPos(btnQuick2.left() - btnInventory.width(), y);
		} else {			
			btnInventory.setPos(btnQuick1.left() - btnInventory.width(), y);
		}
	}

	public void updateLayout() {
		layout();
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

		if (!Dungeon.hero.isAlive()) {
			btnInventory.enable(true);
		}
	}

	public void pickup(Item item) {
		pickedUp.reset(item, btnInventory.centerX(), btnInventory.centerY());
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

	private static class QuickslotTool extends Tool {

		private QuickSlot slot;

		public QuickslotTool(int x, int y, int width, int height) {
			super(x, y, width, height);
		}

		@Override
		protected void createChildren() {
			super.createChildren();

			slot = new QuickSlot();
			add(slot);
		}

		@Override
		protected void layout() {
			super.layout();
			slot.setRect(x + 1, y + 2, width - 2, height - 2);
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

	private static class PickedUpItem extends ItemSprite {

		private static final float DISTANCE = DungeonTilemap.SIZE;
		private static final float DURATION = 0.2f;

		private float dstX;
		private float dstY;
		private float left;

		public PickedUpItem() {
			super();

			originToCenter();

			active = setVisible(false);
		}

		public void reset(Item item, float dstX, float dstY) {
			view(item);

			active = setVisible(true);

			this.dstX = dstX - ItemSprite.SIZE / 2;
			this.dstY = dstY - ItemSprite.SIZE / 2;
			left = DURATION;

			x = this.dstX - DISTANCE;
			y = this.dstY - DISTANCE;
			alpha(1);
		}

		@Override
		public void update() {
			super.update();

			if ((left -= Game.elapsed) <= 0) {

				setVisible(active = false);

			} else {
				float p = left / DURATION;
				scale.set((float) Math.sqrt(p));
				float offset = DISTANCE * p;
				x = dstX - offset;
				y = dstY - offset;
			}
		}
	}
}
