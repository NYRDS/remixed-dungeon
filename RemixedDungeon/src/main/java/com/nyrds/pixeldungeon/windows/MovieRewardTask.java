package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.npc.ServiceManNPC;
import com.nyrds.pixeldungeon.support.Ads;
import com.nyrds.pixeldungeon.utils.ItemsList;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.windows.WndMessage;

public class MovieRewardTask implements Runnable {
    private boolean needReward;

    public MovieRewardTask(boolean result) {
        needReward = result;
    }

    @Override
    public void run() {
        GameScene.show(new WndMessage(StringsManager.getVar(R.string.WndMovieTheatre_Thank_You)) {

            @Override
            public void destroy() {
                super.destroy();

                Hero.movieRewardPending = false;

                if(needReward) {
                    GLog.p(StringsManager.getVar(R.string.WndMovieTheatre_Ok));
                    if(Hero.movieRewardPrize != null) {
                        Dungeon.hero.collect(Hero.movieRewardPrize);
                        Hero.movieRewardPrize = ItemsList.DUMMY;
                    }

                    ServiceManNPC.filmsSeen++;
                    EventCollector.logCountedEvent("ad_reward5",  6);
                    EventCollector.logCountedEvent("ad_reward10", 11);
                    EventCollector.logCountedEvent("ad_reward25", 26);
                    EventCollector.logCountedEvent("ad_reward50", 51);
                    EventCollector.logCountedEvent("ad_reward100", 101);
                    needReward = false;
                } else {
                    GLog.n(StringsManager.getVar(R.string.WndMovieTheatre_Sorry));
                }

                if (GamePreferences.donated() == 0) {
                    if (GameLoop.getDifficulty() == 0) {
                        Ads.displayEasyModeBanner();
                    }
                }

            }
        });
    }
}
