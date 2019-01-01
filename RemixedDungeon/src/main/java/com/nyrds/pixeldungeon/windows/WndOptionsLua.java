package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.mechanics.LuaScript;
import com.watabou.pixeldungeon.windows.WndOptions;

public class WndOptionsLua extends WndOptions {

    private String script;

    public WndOptionsLua(String script,String title, String message, String... options) {
        super(title, message, options);
        this.script = script;
    }

    @Override
    protected void onSelect(int index) {
        LuaScript script = new LuaScript(this.script, this);
        script.run("onSelect",index);
    }
}
