package com.nyrds.pixeldungeon.items;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.items.EquipableItem;

import org.jetbrains.annotations.NotNull;

public class DummyItem extends EquipableItem {
    {
        deactivateActor();
    }
    @Override
    public Belongings.Slot slot(Belongings belongings) {
        return Belongings.Slot.NONE;
    }

    @Override
    public boolean dontPack() {
        return true;
    }

    @Override
    protected boolean doUnequip(Char hero, boolean collect, boolean single) {
        return true;
    }

    @Override
    public boolean goodForMelee() {
        return false;
    }

    @Override
    public void setCursed(boolean cursed) {
    }

    @Override
    public String getVisualName() {
        return "none";
    }

    @Override
    public void doDrop(@NotNull Char hero) {
    }

    @Override
    public int requiredSTR() {
        return super.requiredSTR();
    }

    @Override
    public int quantity() {
        return 0;
    }

    @Override
    public boolean valid() {
        return false;
    }

    @Override
    public void activate(@NotNull Char ch) {
    }

    @Override
    public String getEntityKind() {
        return "DummyItem";
    }

    @Override
    public boolean doPickUp(@NotNull Char hero) {
        return false;
    }
}
