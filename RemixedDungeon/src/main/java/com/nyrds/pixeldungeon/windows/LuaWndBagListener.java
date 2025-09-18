package com.nyrds.pixeldungeon.windows;

import com.watabou.pixeldungeon.windows.WndBag;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.actors.Char;
import com.nyrds.platform.game.RemixedDungeon;

public class LuaWndBagListener implements WndBag.Listener {
    private int callbackId;

    public LuaWndBagListener(int callbackId) {
        this.callbackId = callbackId;
    }

    @Override
    public void onSelect(Item item, Char selector) {
        // Call the Lua callback function with the item and selector
        RemixedDungeon.luaCallByGlobalId(callbackId, item, selector);
    }
}