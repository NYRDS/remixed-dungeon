package com.nyrds.pixeldungeon.ai;

import com.nyrds.pixeldungeon.mechanics.LuaScript;
import com.watabou.noosa.StringsManager;
import com.watabou.pixeldungeon.actors.mobs.Mob;

public class CustomMobAi extends MobAi implements AiState {

    String scriptFile;

    private LuaScript script;

    CustomMobAi(String scriptFile) {
        this.scriptFile = scriptFile;
        script = new LuaScript("scripts/ai/"+scriptFile, this);
    }

    @Override
    public void act(Mob me) {
        script.run("act",me);
    }

    @Override
    public void gotDamage(Mob me, Object src, int dmg) {
        script.run("gotDamage",me, src, dmg);
    }

    @Override
    public String getTag() {
        return scriptFile;
    }

    @Override
    public String status(Mob me) {
        script.run("status", me);
        return StringsManager.maybeId(script.getResult().toString());
    }
}
