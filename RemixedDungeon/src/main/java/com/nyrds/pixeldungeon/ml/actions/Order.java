package com.nyrds.pixeldungeon.ml.actions;

import static com.watabou.pixeldungeon.actors.Actor.TICK;

import com.watabou.pixeldungeon.actors.Char;

import org.jetbrains.annotations.NotNull;

public class Order extends CharAction {
    private final Char target;

    public Order(@NotNull Char target) {
        this.target = target;
        dst = target.getPos();
    }

    @Override
    public boolean act(Char hero) {
        hero.spend(TICK/10);
        hero.setCurAction(null);
        hero.busy();
        hero.selectCell(new OrderCellSelector(target));
        return false;
    }
}
