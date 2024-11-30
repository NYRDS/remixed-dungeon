package com.nyrds.pixeldungeon.ml.actions;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.DungeonGenerator;
import com.nyrds.pixeldungeon.utils.Position;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Rankings;
import com.watabou.pixeldungeon.ResultDescriptions;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Hunger;
import com.watabou.pixeldungeon.items.Amulet;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.InterlevelScene;
import com.watabou.pixeldungeon.scenes.SurfaceScene;
import com.watabou.pixeldungeon.windows.WndMessage;

public class Ascend extends CharAction {
    public Ascend(int stairs ) {
        this.dst = stairs;
    }

    @Override
    public boolean act(Char hero) {
        Level level = hero.level();
        int heroPos = hero.getPos();
        if ((heroPos == dst || level.adjacent(heroPos, dst)) && dst == level.getEntrance()) {
            hero.setPos(dst);
            Position nextLevel = DungeonGenerator.ascend(Dungeon.currentPosition());

            if (nextLevel.levelId.equals("0")) {

                if (hero.getBelongings().getItem(Amulet.class) == null) {
                    GameScene.show(new WndMessage(StringsManager.getVar(R.string.Hero_Leave)));
                    hero.readyAndIdle();
                } else {
                    Dungeon.win(ResultDescriptions.getDescription(ResultDescriptions.Reason.WIN), Rankings.gameOver.WIN_HAPPY);
                    Dungeon.gameOver();
                    GameLoop.switchScene(SurfaceScene.class);
                }
            } else {
                hero.clearActions();
                if (!level.isSafe()) {
                    hero.hunger().satisfy(-Hunger.STARVING / 10);
                }
                InterlevelScene.Do(InterlevelScene.Mode.ASCEND);
            }
            return false;
        }

        if (hero.getCloser(dst)) {
            return true;
        }

        hero.readyAndIdle();
        return false;
    }
}
