package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.npc.ServiceManNPC;
import com.nyrds.pixeldungeon.support.Ads;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.RemixedDungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.windows.WndMessage;

import lombok.var;

public class MovieRewardTask implements Runnable {
    private boolean needReward;

    public MovieRewardTask(boolean result) {
        needReward = result;
    }

    @Override
    public void run() {
        GameScene.show(new WndMessage(Game.getVar(R.string.WndMovieTheatre_Thank_You) ) {

            @Override
            public void destroy() {
                super.destroy();
                var serviceMan = new ServiceManNPC();

                Hero.movieRewardPending = false;

                if(needReward) {
                    serviceMan.say(Game.getVar(R.string.WndMovieTheatre_Ok));
                    ServiceManNPC.reward();
                    EventCollector.logCountedEvent("ad_reward5",  6);
                    EventCollector.logCountedEvent("ad_reward10", 11);
                    needReward = false;
                } else {
                    serviceMan.say(Game.getVar(R.string.WndMovieTheatre_Sorry));
                }

                if (RemixedDungeon.donated() == 0) {
                    if (RemixedDungeon.getDifficulty() == 0) {
                        Ads.displayEasyModeBanner();
                    }
                }

            }
        });
    }
}
