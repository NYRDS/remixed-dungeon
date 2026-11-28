package com.nyrds.pixeldungeon.ai;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.mechanics.LuaScript;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.utils.GLog;
import org.jetbrains.annotations.NotNull;

public class CustomMobAi extends MobAi implements AiState {

    final String scriptFile;

    private final LuaScript script;

    CustomMobAi(String scriptFile) {
        this.scriptFile = scriptFile;
        script = new LuaScript("scripts/ai/" +scriptFile, this);
    }

    // a broken mob AI must not kill the game: log and skip the turn instead of
    // letting the error escape into the update loop (fatal on the desktop runner)
    private void actGuarded(Char me) {
        try {
            script.run("act", me);
        } catch (Exception e) {
            EventCollector.logException(e, "CustomMobAi:" + scriptFile + ":act");
            GLog.toFile("mob ai error: script=%s handler=act : %s", scriptFile, e.getMessage());
        }
    }

    @Override
    public void act(@NotNull Char me) {
        actGuarded(me);
    }

    @Override
    public void gotDamage(Char me, NamedEntityKind src, int dmg) {
        try {
            script.run("gotDamage", me, src, dmg);
        } catch (Exception e) {
            EventCollector.logException(e, "CustomMobAi:" + scriptFile + ":gotDamage");
            GLog.toFile("mob ai error: script=%s handler=gotDamage : %s", scriptFile, e.getMessage());
        }
    }

	@Override
	@LuaInterface
	public String getTag() {
		return scriptFile;
	}

    @Override
    public String status(Char me) {
        return StringsManager.maybeId(script.runOptional("status","CustomAi:"+scriptFile, me));
    }
}
