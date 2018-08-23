package com.nyrds.pixeldungeon.ai;

import com.nyrds.pixeldungeon.mechanics.LuaScript;
import com.watabou.pixeldungeon.actors.mobs.Mob;

public class CustomMobAi extends MobAi implements AiState {

    String scriptFile;

    private LuaScript script;

    CustomMobAi(String scriptFile) {
        this.scriptFile = scriptFile;
        script = new LuaScript("scripts/mobs/ai/"+scriptFile, this);
    }

    @Override
    public boolean act(Mob me) {
        script.run("act",me);
        return true;
    }

    @Override
    public void gotDamage(Mob me, Object src, int dmg) {
        script.run("gotDamage",me, src, dmg);
    }

    @Override
    public String getTag() {
        return scriptFile;
    }
}
