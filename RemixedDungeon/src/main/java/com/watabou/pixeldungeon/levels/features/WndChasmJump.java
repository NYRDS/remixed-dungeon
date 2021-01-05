package com.watabou.pixeldungeon.levels.features;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.windows.WndOptions;

class WndChasmJump extends WndOptions {
    private final Hero hero;

    public WndChasmJump(Hero hero) {
        super(Game.getVar(R.string.Chasm_Chasm), Game.getVar(R.string.Chasm_Jump), Game.getVar(R.string.Chasm_Yes), Game.getVar(R.string.Chasm_No));
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
