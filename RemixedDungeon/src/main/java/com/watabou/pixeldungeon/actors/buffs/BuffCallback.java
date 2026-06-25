package com.watabou.pixeldungeon.actors.buffs;

import com.nyrds.LuaInterface;

@LuaInterface


public interface BuffCallback {
    void onBuff(CharModifier modifier);
}
