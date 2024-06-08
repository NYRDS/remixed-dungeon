
package com.watabou.pixeldungeon.items;


import com.nyrds.LuaInterface;
import com.nyrds.Packable;
import com.nyrds.pixeldungeon.items.Treasury;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.ItemsList;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Statistics;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.actors.buffs.Frost;
import com.watabou.pixeldungeon.actors.mobs.Mimic;
import com.watabou.pixeldungeon.actors.mobs.Wraith;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.Effects;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.effects.Splash;
import com.watabou.pixeldungeon.effects.particles.FlameParticle;
import com.watabou.pixeldungeon.effects.particles.ShadowParticle;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.plants.Seed;
import com.watabou.pixeldungeon.sprites.Glowing;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import lombok.Getter;

public class Heap implements Bundlable, NamedEntityKind  {

    private static final int SEEDS_TO_POTION = 3;

    @Override
    public String getEntityKind() {
        return getClass().getSimpleName();
    }

    @Override
    public String name() {
        return getEntityKind();
    }

    public enum Type {
        HEAP,
        CHEST,
        LOCKED_CHEST,
        CRYSTAL_CHEST,
        TOMB,
        SKELETON,
        MIMIC
    }

    @NotNull
    @Getter
    public Type type = Type.HEAP;

    public static final Map<Type, Float> regularHeaps = new HashMap<>();

    static {
        regularHeaps.put(Type.SKELETON, 1f);
        regularHeaps.put(Type.CHEST, 4f);
        regularHeaps.put(Type.MIMIC, 1f);
        regularHeaps.put(Type.HEAP, 14f);
    }

    public static final Map<Type, Float> sageHeaps = new HashMap<>();

    static {
        sageHeaps.put(Type.SKELETON, 1f);
        sageHeaps.put(Type.CHEST, 4f);
        sageHeaps.put(Type.HEAP, 14f);
    }

    @Packable
    public int pos = Level.INVALID_CELL;

    @Nullable
    public ItemSprite sprite;

    @NotNull
    public LinkedList<Item> items = new LinkedList<>();

    public String imageFile() {
        if (type == Type.HEAP) {
            return size() > 0 ? items.peek().imageFile() : Assets.ITEMS;
        }
        return Assets.ITEMS;
    }

    public float scale() {
        Item topItem = items.peek();
        if (topItem != null) {
            return topItem.heapScale();
        }
        return 1.f;
    }

    public int image() {
        switch (type) {
            case HEAP:
                return size() > 0 ? items.peek().image() : 0;
            case CHEST:
            case MIMIC:
                return ItemSpriteSheet.CHEST;
            case LOCKED_CHEST:
                return ItemSpriteSheet.LOCKED_CHEST;
            case CRYSTAL_CHEST:
                return ItemSpriteSheet.CRYSTAL_CHEST;
            case TOMB:
                return ItemSpriteSheet.TOMB;
            case SKELETON:
                return ItemSpriteSheet.BONES;
            default:
                return 0;
        }
    }

    public Glowing glowing() {
        return (type == Type.HEAP) && !items.isEmpty() ? items.peek().glowing() : null;
    }

    public void open(Char chr) {
        switch (type) {
            case MIMIC:
                if (Mimic.spawnAt(pos, items) != null) {
                    GLog.n(StringsManager.getVar(R.string.Heap_Mimic));
                    destroy();
                } else {
                    type = Type.CHEST;
                }
                break;
            case TOMB:
                Wraith.spawnAround(chr.getPos());
                break;
            case SKELETON:
                CellEmitter.center(pos).start(Speck.factory(Speck.RATTLE), 0.1f, 3);
                for (Item item : items) {
                    if (item.isCursed()) {
                        if (Wraith.spawnAt(pos) == null) {
                            chr.getSprite().emitter().burst(ShadowParticle.CURSE, 6);
                            chr.damage(chr.hp() / 2, this);
                        }
                        Sample.INSTANCE.play(Assets.SND_CURSED);
                        break;
                    }
                }
                break;
            default:
        }

        if (type != Type.MIMIC) {
            type = Type.HEAP;
            sprite.link();
            sprite.drop();
        }
    }

    public int size() {
        return items.size();
    }

    private Item removeFromHeap(Item item) {
        item.setHeap(null);
        updateHeap();
        return item;
    }

    public Item pickUp() {
        Item item = items.removeFirst();
        return removeFromHeap(item);
    }

    public Item pickUp(Item item) {
        items.remove(item);
        return removeFromHeap(item);
    }

    public void pickUpFailed() {
        if (!isEmpty()) {
            Item item = items.removeFirst();
            items.addLast(item);
        }
        updateHeap();
    }

    @LuaInterface
    @NotNull
    public Item peek() {
        Item item = items.peek();

        if (item != null && item.valid()) {
            return item;
        }

        return ItemsList.DUMMY;
    }

    public void drop(@NotNull Item item) {

        if (!item.valid()) {
            EventCollector.logException("Invalid item");
            return;
        }

        if (items.contains(item)) { //TODO fix me
            return;
        }

        if (item.stackable) {
            String c = item.getEntityKind();
            for (Item i : items) {
                if (i.getEntityKind().equals(c)) {
                    i.quantity(i.quantity() + item.quantity());
                    item = i;
                    break;
                }
            }
            items.remove(item);
        }
        item.setHeap(this);
        items.addFirst(item);

        updateHeap();
    }

    public void replace(Item item, Item newItem) {
        if (newItem == null) {
            item.setHeap(null);
            items.remove(item);
        } else {
            if (!item.equals(newItem)) {
                int index = items.indexOf(item);
                if (index != -1) {
                    items.set(index, newItem);
                    newItem.setHeap(this);
                }
            }
        }
    }

    public void updateHeap() {
        if (isEmpty()) {
            destroy();
        } else {
            if (sprite != null) {
                float scale = scale();
                sprite.setScaleXY(scale, scale);
                if (type == Type.HEAP) {
                    sprite.view(peek());
                } else {
                    sprite.view(imageFile(), image(), glowing());
                }
                sprite.place(pos);
            }
        }
    }

    public void burn() {

        if (type == Type.MIMIC) {
            Mimic m = Mimic.spawnAt(pos, items);
            if (m != null) {
                Buff.affect(m, Burning.class).reignite(m);
                m.getSprite().emitter().burst(FlameParticle.FACTORY, 5);
                destroy();
            }
        }
        if (type != Type.HEAP) {
            return;
        }

        boolean burnt = false;
        boolean evaporated = false;

        for (Item item : items.toArray(new Item[0])) {
            Item burntItem = item.burn(pos);

            if (!item.equals(burntItem) && !(item instanceof Dewdrop)) {
                burnt = true;
            }

            if (item instanceof Dewdrop) {
                evaporated = true;
            }

            replace(item, burntItem);
        }

        if (burnt || evaporated) {

            if (Dungeon.isCellVisible(pos)) {
                if (burnt) {
                    Effects.burnFX(pos);
                } else {
                    Effects.evaporateFX(pos);
                }
            }
        }

        updateHeap();
    }

    public void freeze() {
        if (type == Type.MIMIC) {
            Mimic m = Mimic.spawnAt(pos, items);
            if (m != null) {
                Buff.prolong(m, Frost.class, Frost.duration(m) * Random.Float(1.0f, 1.5f));
                destroy();
            }
        }
        if (type != Type.HEAP) {
            return;
        }

        for (Item item : items.toArray(new Item[0])) {
            Item frozenItem = item.freeze(pos);

            replace(item, frozenItem);
        }

        updateHeap();
    }

    public void poison() {
        if (type == Type.MIMIC) {
            Mimic m = Mimic.spawnAt(pos, items);
            if (m != null) {
                destroy();
            }
        }
        if (type != Type.HEAP) {
            return;
        }

        for (Item item : items.toArray(new Item[0])) {
            Item toxicatedItem = item.poison(pos);

            replace(item, toxicatedItem);
        }

        updateHeap();
    }

    public Item transmute() {

        CellEmitter.get(pos).burst(Speck.factory(Speck.BUBBLE), 3);
        Splash.at(pos, 0xFFFFFF, 3);

        float[] chances = new float[items.size()];
        int count = 0;

        int index = 0;
        for (Item item : items) {
            if (item instanceof Seed) {
                count += item.quantity();
                chances[index++] = item.quantity();
            } else {
                count = 0;
                break;
            }
        }

        if (count >= SEEDS_TO_POTION) {

            CellEmitter.get(pos).burst(Speck.factory(Speck.WOOL), 6);
            Sample.INSTANCE.play(Assets.SND_PUFF);

            if (Random.Int(count) == 0) {

                CellEmitter.center(pos).burst(Speck.factory(Speck.EVOKE), 3);

                destroy();

                Statistics.potionsCooked++;
                Badges.validatePotionsCooked();

                return Treasury.getLevelTreasury().random(Treasury.Category.POTION);

            } else {

                Seed proto = (Seed) items.get(Random.chances(chances));
                Class<? extends Item> itemClass = proto.alchemyClass;

                destroy();

                Statistics.potionsCooked++;
                Badges.validatePotionsCooked();

                if (itemClass == null) {
                    return Treasury.getLevelTreasury().random(Treasury.Category.POTION);
                } else {
                    try {
                        return itemClass.newInstance();
                    } catch (Exception e) {
                        return ItemsList.DUMMY;
                    }
                }
            }

        } else {
            return ItemsList.DUMMY;
        }
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void destroy() {
        Dungeon.level.removeHeap(pos);
        if (sprite != null) {
            sprite.killAndErase();
        }
        for (Item item : items) {
            item.setHeap(null);
        }
        items.clear();
    }

    private static final String TYPE = "type";
    private static final String ITEMS = "items";

    @Override
    public void restoreFromBundle(Bundle bundle) {
        try {
            type = Type.valueOf(bundle.getString(TYPE));
        } catch (Throwable e) {
            EventCollector.logException(e);
            type = Type.HEAP;
        }
        items = new LinkedList<>(bundle.getCollection(ITEMS, Item.class));

        for(Item item  : items)  {
            item.setHeap(this);
        }
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        bundle.put(TYPE, type.toString());
        bundle.put(ITEMS, items);
    }

    public boolean dontPack() {
        return false;
    }
}
