package com.nyrds.pixeldungeon.ml.actions;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.windows.WndInfoMob;

import org.jetbrains.annotations.NotNull;

public class Examine extends CharAction {
    private final Char target;

    public Examine(@NotNull Char target) {
        this.target = target;
        dst = target.getPos();
    }

    @Override
    public boolean act(Char hero) {
        hero.readyAndIdle();
        GameScene.show(new WndInfoMob(target, hero));
        return false;
    }
}
