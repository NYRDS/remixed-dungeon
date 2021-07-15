package com.watabou.pixeldungeon.levels.features;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.windows.WndOptions;

class WndChasmJump extends WndOptions {
    private final Hero hero;

    public WndChasmJump(Hero hero) {
        super(StringsManager.getVar(R.string.Chasm_Chasm), StringsManager.getVar(R.string.Chasm_Jump), StringsManager.getVar(R.string.Chasm_Yes), StringsManager.getVar(R.string.Chasm_No));
        this.hero = hero;
    }

    @Override
    public void onSelect(int index) {
        if (index == 0) {
            Chasm.jumpConfirmed = true;
            hero.resume();
        }
    }
}
