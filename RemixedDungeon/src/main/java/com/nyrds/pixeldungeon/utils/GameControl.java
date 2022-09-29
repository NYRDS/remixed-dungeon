package com.nyrds.pixeldungeon.utils;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.pixeldungeon.mobs.npc.ServiceManNPC;
import com.nyrds.platform.EventCollector;
import com.nyrds.util.ModdingMode;
import com.nyrds.util.Util;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Logbook;
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
        GameLoop.switchScene(TitleScene.class);
    }

    @LuaInterface
    static public void startNewGame(String className, int difficulty, boolean testMode) {

        GameLoop.setDifficulty(difficulty);

        Dungeon.hero = null;
        Dungeon.level = null;
        Dungeon.heroClass = HeroClass.valueOf(className);

        Dungeon.deleteGame(true);
        Logbook.logbookEntries.clear();    // Clear the log book before starting a new game
        ServiceManNPC.resetLimit();

        if(Util.isDebug()) {
            Hero.performTests();
        }

        if(testMode) {
            EventCollector.disable();
        }

        Map<String,String> resDesc = new HashMap<>();
        resDesc.put("class",className);
        resDesc.put("mod", GamePreferences.activeMod());
        resDesc.put("modVersion", String.valueOf(ModdingMode.activeModVersion()));
        resDesc.put("difficulty",  String.valueOf(difficulty));

        resDesc.put("version", GameLoop.version);
        resDesc.put("donation",Integer.toString(GamePreferences.donated()));

        EventCollector.logEvent("game", resDesc);


        if (GamePreferences.intro()) {
            GamePreferences.intro(false);
            GameLoop.switchScene(IntroScene.class);
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
