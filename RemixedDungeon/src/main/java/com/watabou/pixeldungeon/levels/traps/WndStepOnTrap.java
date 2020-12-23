package com.watabou.pixeldungeon.levels.traps;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.windows.WndOptions;

class WndStepOnTrap extends WndOptions {
    private final Hero hero;

    public WndStepOnTrap(Hero hero) {
        super(Game.getVar(R.string.TrapWnd_Title), Game.getVar(R.string.TrapWnd_Step), Game.getVar(R.string.Chasm_Yes), Game.getVar(R.string.Chasm_No));
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
