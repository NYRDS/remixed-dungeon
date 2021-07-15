package com.watabou.pixeldungeon.levels.traps;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.windows.WndOptions;

class WndStepOnTrap extends WndOptions {
    private final Hero hero;

    public WndStepOnTrap(Hero hero) {
        super(StringsManager.getVar(R.string.TrapWnd_Title), StringsManager.getVar(R.string.TrapWnd_Step), StringsManager.getVar(R.string.Chasm_Yes), StringsManager.getVar(R.string.Chasm_No));
        this.hero = hero;
    }

    @Override
    public void onSelect(int index) {
        if (index == 0) {
            TrapHelper.stepConfirmed = true;
            hero.resume();
        }
    }
}
