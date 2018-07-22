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

import android.support.annotation.Nullable;

import com.nyrds.pixeldungeon.mechanics.spells.SpellHelper;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.windows.HBox;
import com.nyrds.pixeldungeon.windows.VBox;
import com.nyrds.pixeldungeon.windows.VHBox;
import com.nyrds.pixeldungeon.windows.WndHeroSpells;
import com.watabou.noosa.Game;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.Chrome;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.levels.Level;
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


    private final Tool btnWait;
    private final Tool btnSearch;
    private final Tool btnInfo;

    @Nullable
    private final Tool btnSpells;

    private InventoryTool btnInventory;

    private Component toolbar = new Component();

    public static final int MAX_SLOTS = 25;

    private ArrayList<QuickslotTool> slots = new ArrayList<>();
    final private Hero hero;

    public Toolbar(final Hero hero, float maxWidth) {
        super();

        this.hero = hero;

        btnWait = new Tool(7, Chrome.Type.ACTION_BUTTON) {
            @Override
            protected void onClick() {
                if (hero.isReady()) {
                    hero.rest(false);
                }
            }

            protected boolean onLongClick() {
                if (hero.isReady()) {
                    hero.rest(true);
                }
                return true;
            }
        };

        btnSearch = new Tool(8, Chrome.Type.ACTION_BUTTON) {
            @Override
            protected void onClick() {
                if (hero.isReady()) {
                    hero.search(true);
                }
            }
        };

        btnInfo = new Tool(9, Chrome.Type.ACTION_BUTTON) {
            @Override
            protected void onClick() {
                if (hero.isReady()) {
                    GameScene.selectCell(informer);
                }
            }
        };

        btnSpells = new Tool(SpellHelper.iconIdByHero(hero), Chrome.Type.ACTION_BUTTON) {
            @Override
            protected void onClick() {
                if (hero.isReady()) {
                    GameScene.show(new WndHeroSpells(null));
                }
            }
        };

        btnInventory = new InventoryTool();

        for (int i = 0; i < MAX_SLOTS; i++) {
            slots.add(new QuickslotTool());
        }
    }

    @Override
    protected void layout() {

        toolbar.removeAll();
        toolbar.destroy();

        int active_slots = PixelDungeon.quickSlots();

        HBox slotBox = new HBox(width());
        slotBox.setAlign(HBox.Align.Center);

        if(active_slots<0) {
            int slotsCanFit = (int) (width()/slots.get(0).width());
            if(Math.abs(active_slots)!=slotsCanFit) {
                PixelDungeon.quickSlots(-slotsCanFit);
                return;
            }
            active_slots = slotsCanFit;
        }

        for (int i = 0; i < active_slots; i++) {
            slots.get(i).show(true);
            slotBox.add(slots.get(i));
        }

        if (slotBox.width() > width()) {
            PixelDungeon.quickSlots(active_slots - 1);
            return;
        }


        VHBox actionBox = new VHBox(width());
        actionBox.add(btnWait);
        actionBox.add(btnSearch);
        actionBox.add(btnInfo);
        actionBox.setAlign(VBox.Align.Bottom);

        VHBox inventoryBox = new VHBox(width());
        if (hero.spellUser) {
            inventoryBox.add(btnSpells);
        }
        inventoryBox.add(btnInventory);
        inventoryBox.setAlign(VBox.Align.Bottom);

        boolean handness = PixelDungeon.handedness();

        actionBox.setAlign(handness ? HBox.Align.Right : HBox.Align.Left);
        inventoryBox.setAlign(handness ? HBox.Align.Right : HBox.Align.Left);

        if (slotBox.width() + actionBox.width() + inventoryBox.width() > width()) {
            actionBox.setMaxWidth(0);
            inventoryBox.setMaxWidth(0);
            actionBox.reset();
            inventoryBox.reset();
        }

        if (slotBox.width() + actionBox.width() + inventoryBox.width() > width()) {
            actionBox.setMaxWidth(width());
            inventoryBox.setMaxWidth(width());
            actionBox.reset();
            inventoryBox.reset();

            actionBox.wrapContent();
            inventoryBox.wrapContent();

            toolbar = new VBox();

            HBox buttonsBox = new HBox(width());
            buttonsBox.setAlign(HBox.Align.Width);
            buttonsBox.setAlign(VBox.Align.Bottom);

            if (handness) {
                buttonsBox.add(inventoryBox);
                buttonsBox.add(actionBox);
            } else {
                buttonsBox.add(actionBox);
                buttonsBox.add(inventoryBox);
            }

            toolbar.add(slotBox);
            toolbar.add(buttonsBox);

            ((VBox) toolbar).setAlign(VBox.Align.Bottom);
        } else {
            toolbar = new HBox(width());

            actionBox.wrapContent();
            inventoryBox.wrapContent();
            slotBox.wrapContent();

            if (handness) {
                toolbar.add(inventoryBox);
                toolbar.add(slotBox);
                toolbar.add(actionBox);
            } else {
                toolbar.add(actionBox);
                toolbar.add(slotBox);
                toolbar.add(inventoryBox);
            }

            ((HBox) toolbar).setAlign(HBox.Align.Width);
            ((HBox) toolbar).setAlign(VBox.Align.Bottom);
        }

        toolbar.setRect(x, camera.height - toolbar.height(), camera.width, toolbar.height());
        add(toolbar);
    }

    private static CellSelector.Listener informer = new CellSelector.Listener() {
        @Override
        public void onSelect(Integer cell) {
            if (cell == null) {
                return;
            }

            Level level = Dungeon.level;

            if (!level.cellValid(cell)
                    || (!level.visited[cell] && !level.mapped[cell])) {
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
            slot.setRect(base.x, base.y, base.width(), base.height());
        }

        public void show(boolean value) {
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
        return toolbar.top();
    }

}
