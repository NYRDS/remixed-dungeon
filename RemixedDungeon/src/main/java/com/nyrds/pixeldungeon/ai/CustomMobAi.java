package com.nyrds.pixeldungeon.ai;

import com.nyrds.pixeldungeon.mechanics.LuaScript;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;

import org.jetbrains.annotations.NotNull;

public class CustomMobAi extends MobAi implements AiState {

    final String scriptFile;

    private final LuaScript script;

    CustomMobAi(String scriptFile) {
        this.scriptFile = scriptFile;
        script = new LuaScript("scripts/ai/"+scriptFile, this);
    }

    @Override
    public void act(@NotNull Char me) {
        script.run("act",me);
    }

    @Override
    public void gotDamage(Char me, NamedEntityKind src, int dmg) {
        script.run("gotDamage",me, src, dmg);
    }

    @Override
    public String getTag() {
        return scriptFile;
    }

    @Override
    public String status(Char me) {
        return StringsManager.maybeId(script.runOptional("status","CustomAi:"+scriptFile, me));
    }
}
