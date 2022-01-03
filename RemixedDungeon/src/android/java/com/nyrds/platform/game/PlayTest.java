package com.nyrds.platform.game;

import com.nyrds.pixeldungeon.utils.GameControl;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.utils.Utils;

public class PlayTest extends RemixedDungeon{

    public PlayTest() {
    }

    @Override
    public void onResume() {
        super.onResume();
        GameControl.startNewGame(Utils.randomEnum(HeroClass.class).name(), 2, true);
    }
}
