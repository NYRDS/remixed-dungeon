package com.nyrds.pixeldungeon.utils;

public class EntityIdSource {
    static int lastUsedId;

    public static synchronized int getNextId() {
        lastUsedId++;
        return lastUsedId;
    }

    public static synchronized void setLastUsedId(int id) {
        lastUsedId = id;
    }
}
