package com.nyrds.pixeldungeon.mobs.npc;

import com.nyrds.android.util.Util;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.support.PollfishSurveys;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.windows.WndQuest;

public class InquirerNPC extends ImmortalNPC {

    public InquirerNPC() {
        if (PollfishSurveys.consented()) {
            PollfishSurveys.init();
        }
    }


    public static void reward() {
    }

    @Override
    public boolean interact(final Hero hero) {

        if (!PollfishSurveys.consented()) {
        }

        getSprite().turnTo(getPos(), hero.getPos());

        if (!Util.isConnectedToInternet()) {
            GameScene.show(new WndQuest(this, Game.getVar(R.string.ServiceManNPC_NoConnection)));
            return true;
        }

        PollfishSurveys.showSurvey();

        return true;
    }
}
