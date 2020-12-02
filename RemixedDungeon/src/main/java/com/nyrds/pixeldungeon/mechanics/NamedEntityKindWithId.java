package com.nyrds.pixeldungeon.mechanics;

public interface NamedEntityKindWithId extends NamedEntityKind {
    int getId();
    boolean valid();
}
