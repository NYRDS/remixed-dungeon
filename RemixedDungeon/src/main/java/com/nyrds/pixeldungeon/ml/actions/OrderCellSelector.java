package com.nyrds.pixeldungeon.ml.actions;

import com.nyrds.pixeldungeon.ai.KillOrder;
import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.ai.MoveOrder;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.scenes.CellSelector;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;

class OrderCellSelector implements CellSelector.Listener {
    private final Char target;

    public OrderCellSelector(Char target) {
        this.target = target;

        CharUtils.mark(target);
        CharUtils.markTarget(target);
    }

    @Override
    public void onSelect(Integer cell, @NotNull Char selector) {
        selector.next();
        CharUtils.clearMarkers();

        if (cell == null) {
            return;
        }

        CharAction action = CharUtils.actionForCell(target, cell, target.level());

        if (action instanceof Move) {
            target.setState(MobAi.getStateByClass(MoveOrder.class));
            target.setTarget(cell);
            return;
        }

        if (action instanceof Attack) {
            Attack attack = (Attack) action;
            target.setState(MobAi.getStateByClass(KillOrder.class));
            target.setEnemy(attack.target);
            return;
        }

        target.say(Utils.format(R.string.Mob_CantDoIt));
    }

    @Override
    public String prompt() {
        return Utils.capitalize(Utils.format(R.string.Mob_ReadyForOrder, target.getName()));
    }

    @Override
    public Image icon() {
        return target.getSprite().avatar();
    }
}
