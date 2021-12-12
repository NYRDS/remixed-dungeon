package com.nyrds.platform.game;

import com.watabou.noosa.Scene;

public class QuickModTest extends RemixedDungeon{

    public QuickModTest() {
    }

    @Override
    public void onResume() {
        super.onResume();
        Scene.setMode(Scene.LEVELS_TEST);
    }
}
