package com.nyrds.lua;

import org.luaj.vm2.LuaValue;

public interface LuaEntryAction {
    void apply(LuaValue key, LuaValue val);
}
