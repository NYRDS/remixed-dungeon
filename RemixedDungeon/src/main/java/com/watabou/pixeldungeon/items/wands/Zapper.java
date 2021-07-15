package com.watabou.pixeldungeon.items.wands;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.scenes.CellSelector;
import com.watabou.pixeldungeon.utils.GLog;

import org.jetbrains.annotations.NotNull;

class Zapper implements CellSelector.Listener {
    private final Wand wand;

    public Zapper(Wand wand) {
        this.wand = wand;
    }

    @Override
    public void onSelect(Integer target, @NotNull Char selector) {

        if (target != null) {
            if (target == selector.getPos()) {
                GLog.i(StringsManager.getVar(R.string.Wand_SelfTarget));
                return;
            }

            final int cell = wand.getDestinationCell(selector.getPos(),target);
            selector.getSprite().zap(cell);
            wand.wandEffect(cell, selector);
        }
    }

    @Override
    public String prompt() {
        return StringsManager.getVar(R.string.Wand_Prompt);
    }

    @Override
    public Image icon() {
        return null;
    }
}
