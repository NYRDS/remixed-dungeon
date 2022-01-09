package com.nyrds.pixeldungeon.support;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.windows.MovieRewardTask;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.game.RemixedDungeon;
import com.watabou.noosa.InterstitialPoint;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Item;

@LuaInterface
public class AdsRewardVideo implements InterstitialPoint {

    @LuaInterface
    public void show(Item prize) {
        Hero.movieRewardPrize = prize;

        GameLoop.softPaused = true;
        Dungeon.save(false);

        GameLoop.pushUiTask( () -> {
            Dungeon.save(true);
            GameLoop.softPaused = false;

            Game.runOnMainThread(() -> {
                Ads.removeEasyModeBanner();
                Ads.showRewardVideo(this);
            });
        });
    }

    @Override
    public void returnToWork(final boolean result) {

        Hero.movieRewardPending = result;

        GameLoop.pushUiTask(() -> {

            Dungeon.save(true);
            GameLoop.softPaused = false;
            Hero.doOnNextAction = new MovieRewardTask(result);

            RemixedDungeon.landscape(RemixedDungeon.storedLandscape());
            GameLoop.setNeedSceneRestart();
        });
    }

    @LuaInterface
    public boolean isReady() {
        return AdsUtilsCommon.isRewardVideoReady();
    }
}
