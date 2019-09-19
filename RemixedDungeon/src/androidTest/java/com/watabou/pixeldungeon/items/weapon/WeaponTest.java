package com.watabou.pixeldungeon.items.weapon;

import androidx.test.filters.SmallTest;
import androidx.test.runner.AndroidJUnit4;

import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.weapon.melee.ShortSword;
import com.watabou.utils.Bundle;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class WeaponTest {

    @Test
    public void SaveRestore() {
        Weapon item = new ShortSword();
        item.upgrade(10);
        item.upgrade(true);

        Bundle bundle = new Bundle();

        bundle.put("a", item);

        Item restoredItem = (Item) bundle.get("a");

        Assert.assertEquals(item,restoredItem);
    }

}