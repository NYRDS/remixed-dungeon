package com.watabou.pixeldungeon.items;

public class PseudoItem extends Item {
    @Override
    public String getEntityKind() {
        return getClass().getSimpleName();
    }
}
