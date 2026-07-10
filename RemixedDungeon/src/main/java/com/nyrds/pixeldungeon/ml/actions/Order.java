package com.nyrds.pixeldungeon.ml.actions;

import com.watabou.pixeldungeon.actors.Char;

import org.jetbrains.annotations.NotNull;

public class Order extends CharAction {
    private final Char target;

    public Order(@NotNull Char target) {
        this.target = target;
        dst = target.getPos();
    }

    @Override
    public void act(Char hero) {
        hero.setCurAction(null);
        hero.selectCell(new OrderCellSelector(target));
        hero.spend(Char.TICK/hero.speed()/50);
    }
}
