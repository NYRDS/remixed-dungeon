package com.nyrds.pixeldungeon.utils;

import com.nyrds.LuaInterface;
import com.nyrds.android.util.ModdingMode;
import com.nyrds.android.util.Util;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.mobs.npc.ServiceManNPC;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Logbook;
import com.watabou.pixeldungeon.RemixedDungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.scenes.InterlevelScene;
import com.watabou.pixeldungeon.scenes.IntroScene;
import com.watabou.pixeldungeon.scenes.TitleScene;

import java.util.HashMap;
import java.util.Map;

public class GameControl {

    @LuaInterface
    static public void titleScene() {
        Game.switchScene(TitleScene.class);
    }

    @LuaInterface
    static public void startNewGame(String className, int difficulty, boolean testMode) {
        Dungeon.setDifficulty(difficulty);
        Dungeon.hero = null;
        Dungeon.level = null;
        Dungeon.heroClass = HeroClass.valueOf(className);

        if(Util.isDebug()) {
            Hero.performTests();
        }

        if(testMode) {
            EventCollector.disable();
        }

        Map<String,String> resDesc = new HashMap<>();
        resDesc.put("class",className);
        resDesc.put("mod", RemixedDungeon.activeMod());
        resDesc.put("modVersion", String.valueOf(ModdingMode.activeModVersion()));
        resDesc.put("difficulty",  String.valueOf(difficulty));

        resDesc.put("version", Game.version);
        resDesc.put("donation",Integer.toString(RemixedDungeon.donated()));

        EventCollector.logEvent("game", resDesc);

        Logbook.logbookEntries.clear();    // Clear the log book before starting a new game
        ServiceManNPC.resetLimit();

        if (RemixedDungeon.intro()) {
            RemixedDungeon.intro(false);
            Game.switchScene(IntroScene.class);
        } else {
            InterlevelScene.Do(InterlevelScene.Mode.DESCEND);
        }
    }

    @LuaInterface
    static public void changeLevel(String levelId) {
        InterlevelScene.returnTo = new Position(levelId,-1);
        InterlevelScene.Do(InterlevelScene.Mode.RETURN);
    }
}
