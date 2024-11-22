package com.nyrds.pixeldungeon.mobs.npc;

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.support.Ads;
import com.nyrds.pixeldungeon.support.AdsUtils;
import com.nyrds.pixeldungeon.windows.WndMovieTheatre;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.util.Os;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndQuest;

public class ServiceManNPC extends ImmortalNPC {

    private static final int BASIC_GOLD_REWARD = 150;

    @Packable
    public static int filmsSeen = 0;

    public ServiceManNPC() {
        AdsUtils.initRewardVideo();
    }

    public static Item getReward() {
        return new Gold(BASIC_GOLD_REWARD + (filmsSeen / 5) * 50);
    }


    @Override
    public boolean interact(final Char hero) {
        getSprite().turnTo(getPos(), hero.getPos());

        if (!Os.isConnectedToInternet()) {
            GameScene.show(new WndQuest(this, StringsManager.getVar(R.string.ServiceManNPC_NoConnection)));
            return true;
        }

        if (filmsSeen >= getLimit()) {
            GameScene.show(new WndQuest(this, Utils.format(R.string.ServiceManNPC_Limit_Reached, getLimit())));
            return true;
        }

        GameLoop.pushUiTask( () -> {
            GameLoop.runOnMainThread(() ->
            {
                boolean result = Ads.isRewardVideoReady();
                GameLoop.pushUiTask(() -> {
                            if (result) {
                                GameScene.show(new WndMovieTheatre(this, filmsSeen, getLimit()));
                            } else {
                                Game.softPaused = false;
                                say(StringsManager.getVar(R.string.ServiceManNPC_NotReady));
                            }
                        }
                );
            });
        });

        return true;
    }

    private static int getLimit() {
        return 4 + Dungeon.hero.lvl();
    }

    public static void resetLimit() {
        filmsSeen = 0;
    }
}
