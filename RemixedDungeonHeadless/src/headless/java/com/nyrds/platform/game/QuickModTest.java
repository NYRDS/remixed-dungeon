package com.nyrds.platform.game;

import com.watabou.noosa.Scene;

public class QuickModTest extends RemixedDungeon{

    public QuickModTest() {
        super();
    }

    @Override
    public void resume() {
        super.resume();
        Scene.setMode(Scene.LEVELS_TEST);
    }
}
