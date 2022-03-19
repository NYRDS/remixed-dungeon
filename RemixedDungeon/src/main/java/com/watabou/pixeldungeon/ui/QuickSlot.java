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

import static com.watabou.pixeldungeon.scenes.PixelScene.uiCamera;

import android.annotation.SuppressLint;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.mechanics.spells.Spell;
import com.nyrds.pixeldungeon.mechanics.spells.SpellFactory;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.CharsList;
import com.nyrds.pixeldungeon.windows.WndHeroSpells;
import com.nyrds.platform.util.StringsManager;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Button;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.wands.Wand;
import com.watabou.pixeldungeon.items.weapon.Weapon;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndBag;
import com.watabou.utils.Bundle;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class QuickSlot extends Button implements WndBag.Listener, WndHeroSpells.Listener {

    private static final String QUICKSLOT       = "quickslot";

    private static final ArrayList<QuickSlot>     slots   = new ArrayList<>();
    @SuppressLint("UseSparseArrays")
    private static final Map<Integer, Item> qsStorage = new HashMap<>();

    private Item quickslotItem;

    private ItemSlot slot;

    private Image crossB;
    private Image crossM;

    private boolean targeting  = false;
    private Item    lastItem   = null;

    @NotNull
    private Char    lastTarget = CharsList.DUMMY;

    private final int index;

    public QuickSlot() {
        super();
        slots.add(this);

        index = slots.size() - 1;
        if (qsStorage.containsKey(index)) {
            selectItem(qsStorage.get(index), index);
        } else {
            item(quickslotItem);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        slots.clear();
        lastItem = null;
        lastTarget = CharsList.DUMMY;
    }

    @Override
    protected void createChildren() {
        super.createChildren();

        slot = new ItemSlot() {
            @Override
            protected void onClick() {

                if (objectForSlot != null) {
                    updateSlotBySelection();
                    return;
                }

                if (targeting && lastTarget.valid()) {
                    GameScene.handleCell(lastTarget.getPos());
                } else {
                    if (quickslotItem == lastItem) {
                        useTargeting();
                    } else {
                        lastItem = quickslotItem;
                    }
                    if (quickslotItem != null) {

                        if (!Dungeon.hero.isAlive()) {
                            return;
                        }

                        quickslotItem.execute(Dungeon.hero);
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

        crossB.setX(PixelScene.align(x + (width - crossB.width) / 2));
        crossB.setY(PixelScene.align(y + (height - crossB.height) / 2));
    }


    private void updateSlotBySelection() {
        selectItem(objectForSlot, index);
        objectForSlot = null;
        GameLoop.scene().remove(prompt);
    }

    @Override
    protected void onClick() {
        Hero hero = Dungeon.hero;

        if(!hero.isReady()) {
            return;
        }

        if (objectForSlot != null) {
            updateSlotBySelection();
            return;
        }

        GameScene.selectItem(hero, this, WndBag.Mode.QUICKSLOT, StringsManager.getVar(R.string.QuickSlot_SelectedItem));
    }

    @Override
    protected boolean onLongClick() {
        Hero hero = Dungeon.hero;

        if(!hero.isReady()) {
            return true;
        }

        if (hero.isSpellUser()) {
            GameScene.selectSpell(this);
        } else {
            GameScene.selectItem(hero, this, WndBag.Mode.QUICKSLOT, StringsManager.getVar(R.string.QuickSlot_SelectedItem));
        }
        return true;
    }

    private void item(@Nullable Item item) {
        slot.item(item);
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
        slot.enable(quickslotItem != null && quickslotItem.usableByHero());
    }

    private void useTargeting() {

        updateTargetingState();

        if (!targeting) {
            if (lastItem instanceof Wand || lastItem instanceof Weapon) {
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
                lastTarget = CharsList.DUMMY;
            }
        }
    }

    private void updateTargetingState() {
        targeting = lastTarget.valid() && lastTarget.isAlive() && Dungeon.isCellVisible(lastTarget.getPos());
    }

    private void refreshSelf() {
        if(quickslotItem != null && !(quickslotItem instanceof Spell.SpellItem)) {
            Item item;
            final Hero hero = Dungeon.hero;

            Belongings belongings = hero.getBelongings();
            if(quickslotItem.quantity()>0) {
                item = belongings.checkItem(quickslotItem);
            } else {
                item = hero.getItem(quickslotItem.getEntityKind());
            }
            if(item.valid()) {
                quickslotItem = item.quickSlotContent();
            } else {
                quickslotItem = ItemFactory.virtual(quickslotItem);
            }
        }

        if(quickslotItem instanceof Spell.SpellItem) {
            quickslotItem = quickslotItem.quickSlotContent();
        }

        item(quickslotItem);
    }

    private static boolean refreshRequested;

    public static void refresh(Char owner) {
        if(owner!=Dungeon.hero) {
            return;
        }

        refreshRequested = true;
    }

    public static void target(Item item, Char target) {

        Char newTarget = target;

        if(newTarget==null) {
            newTarget = CharsList.DUMMY;
        }

        for (QuickSlot slot : slots) {
            if (item == slot.lastItem && newTarget != Dungeon.hero) {
                slot.lastTarget = newTarget;
                HealthIndicator.instance.target(newTarget);
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

    public static Item getEarlyLoadItem(int n) {
        if (qsStorage.containsKey(n)) {
            return qsStorage.get(n);
        }
        return null;
    }

    public static void cleanStorage() {
        qsStorage.clear();
    }

    private void quickslotItem(Item quickslotItem) {

        if (this.quickslotItem != null) {
            this.quickslotItem.setQuickSlotIndex(-1);
        }

        if (quickslotItem != null) {

            int oldQsIndex = quickslotItem.getQuickSlotIndex();
            if (oldQsIndex >= 0 && oldQsIndex < slots.size()) {
                slots.get(oldQsIndex).quickslotItem(null);
                slots.get(oldQsIndex).refreshSelf();
            }


            quickslotItem.setQuickSlotIndex(index);
        }

        qsStorage.put(index, quickslotItem);
        this.quickslotItem = quickslotItem;
    }

    public static void save(Bundle bundle) {
        ArrayList<String> classes = new ArrayList<>();

        for (int i = 0; i < slots.size(); i++) {
            Item item = qsStorage.get(i);
            if(item != null) {
                classes.add(item.getEntityKind());
            } else {
                classes.add(Utils.EMPTY_STRING);
            }
        }

        bundle.put(QUICKSLOT, classes.toArray(new String[0]));
    }

    public static void restore(Bundle bundle) {
        qsStorage.clear();
        String[] classes = bundle.getStringArray(QUICKSLOT);
        for (int i = 0; i < classes.length; i++) {
            if (!classes[i].isEmpty()) {
                if (SpellFactory.hasSpellForName(classes[i])) {
                    Spell spell = SpellFactory.getSpellByName(classes[i]);
                    selectItem(spell.itemForSlot(), i);
                    continue;
                }
                selectItem(ItemFactory.itemByName(classes[i]).quickSlotContent(), i);
            }
        }
        refresh(Dungeon.hero);
    }

    public static void selectItem(Item object, int n) {
        if (n < slots.size()) {
            QuickSlot slot = slots.get(n);
            slot.quickslotItem(object);
            slot.onSelect(slot.quickslotItem, Dungeon.hero);
        } else {
            qsStorage.put(n, object);
        }
    }

    @Override
    public void onSelect(Item item, Char selector) {
        if (item != null) {
            quickslotItem(item.quickSlotContent());
            refresh(selector);
        }
    }

    @Override
    public void onSelect(Spell.SpellItem spell, Char hero) {
        if (spell != null) {
            quickslotItem(spell);
            refresh(hero);
        }
    }

    private static Item objectForSlot;
    private static Toast  prompt;

    static public void selectSlotFor(Item item) {
        objectForSlot = item;
        prompt = new Toast(StringsManager.getVar(R.string.QuickSlot_SelectSlot)) {
            @Override
            protected void onClose() {
                GameLoop.scene().remove(this);
                objectForSlot = null;

            }
        };
        prompt.camera = uiCamera;
        prompt.setPos((uiCamera.width - prompt.width()) / 2, uiCamera.height - 60);

        GameLoop.addToScene(prompt);
    }

    public Item getQuickslotItem() {
        return quickslotItem;
    }

    @Override
    public void update() {
        super.update();

        if(refreshRequested) {
            if(Dungeon.hero != null) {
                for (QuickSlot slot : slots) {
                    slot.refreshSelf();
                }
            }
            refreshRequested = false;
        }
    }
}
