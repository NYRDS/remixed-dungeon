package com.nyrds.pixeldungeon.ml.actions;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.windows.WndChar;

public class Interact extends CharAction {
    public final Char chr;
    public Interact(Char chr) {
        this.chr = chr;
        dst = chr.getPos();
    }

    public boolean act(Char hero) {

        if (hero.adjacent(chr)) {

            hero.readyAndIdle();
            hero.getSprite().turnTo(hero.getPos(), dst);
            if (!chr.interact(hero)) {
                new Attack(chr).act(hero);
            }
            return false;

        } else {
            if (chr.getOwnerId() == hero.getId()) {
                GameScene.show(new WndChar(chr, hero));
                return false;
            }
        }



        if (Dungeon.level.fieldOfView[chr.getPos()] && hero.getCloser(chr.getPos())) {
            return true;
        }

        hero.readyAndIdle();
        return false;
    }
}
