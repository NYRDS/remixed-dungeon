package com.nyrds.pixeldungeon.utils;

import com.nyrds.LuaInterface;
import com.nyrds.Packable;
import com.nyrds.pixeldungeon.items.DummyItem;
import com.watabou.pixeldungeon.items.EquipableItem;
import com.watabou.pixeldungeon.items.Item;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

@Packable
public class ItemsList {
    // invalid item
    public static final EquipableItem DUMMY = new DummyItem();

    private static ConcurrentHashMap<Integer, Item> itemsMap = new ConcurrentHashMap<>();

    @LuaInterface
    @NotNull
    static public Item getById(int id) {
        Item ret = itemsMap.get(id);
        if(ret == null) {
            return DUMMY;
        }
        return ret;
    }

    static public void add(Item item, int id) {
        itemsMap.put(id,item);
    }

    static public void remove(int id) {
        itemsMap.remove(id);
    }
}
