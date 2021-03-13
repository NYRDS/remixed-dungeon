package com.nyrds.android;

import com.watabou.noosa.Scene;
import com.watabou.pixeldungeon.RemixedDungeon;

public class QuickModTest extends RemixedDungeon{

    public QuickModTest() {
    }

    @Override
    public void onResume() {
        super.onResume();
        Scene.setMode("levelsTest");
    }
}
