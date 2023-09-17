package com.watabou.pixeldungeon.ui;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.scenes.CellSelector;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.windows.WndChar;
import com.watabou.pixeldungeon.windows.WndInfoCell;
import com.watabou.pixeldungeon.windows.WndInfoItem;
import com.watabou.pixeldungeon.windows.WndInfoMob;
import com.watabou.pixeldungeon.windows.WndMessage;

import org.jetbrains.annotations.NotNull;

class InformerCellListener implements CellSelector.Listener {
    @Override
    public void onSelect(Integer cell, @NotNull Char selector) {
        if (cell == null) {
            return;
        }

        Level level = selector.level();

        if (!level.cellValid(cell)
                || (!level.visited[cell] && !level.mapped[cell])) {
            GameScene.show(new WndMessage(StringsManager.getVar(R.string.Toolbar_Info1)));
            return;
        }

        if (!Dungeon.isCellVisible(cell)) {
            GameScene.show(new WndInfoCell(cell));
            return;
        }

        if (cell == Dungeon.hero.getPos()) {
            GameScene.show(new WndChar(Dungeon.hero));
            return;
        }

        Mob mob = (Mob) Actor.findChar(cell);
        if (mob != null) {
            GameScene.show(new WndInfoMob(mob, selector));
            return;
        }

        Heap heap = level.getHeap(cell);
        if (heap != null) {
            GameScene.show(new WndInfoItem(heap));
            return;
        }

        GameScene.show(new WndInfoCell(cell));
    }

    @Override
    public String prompt() {
        return StringsManager.getVar(R.string.Toolbar_Info2);
    }

    @Override
    public Image icon() {
        return null;
    }
}
