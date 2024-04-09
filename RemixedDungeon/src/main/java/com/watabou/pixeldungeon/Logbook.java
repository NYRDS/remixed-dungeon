package com.watabou.pixeldungeon;

import com.nyrds.Packable;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by mike on 06.01.2018.
 * This file is part of Remixed Pixel Dungeon.
 */

public class Logbook {

    private static final int                LOGBOOK_SIZE   = 30;	// Number of log book messages to store
    private static final String             LOGBOOK        = "logbook";

    public static final List<logBookEntry> logbookEntries = new LinkedList<>();

    public static void addPlayerLogMessage(String message, int color) {
        logbookEntries.add( new logBookEntry(message, color) );	// Add the log entry
        if( logbookEntries.size() > LOGBOOK_SIZE) {	// Need to remove an item because max size was reached
            logbookEntries.remove(0 );
        }
    }

    static public void restoreFromBundle(Bundle bundle) {
        logbookEntries.clear();
        logbookEntries.addAll(bundle.getCollection(LOGBOOK, logBookEntry.class));
    }

    static public void storeInBundle(Bundle bundle) {
        bundle.put(LOGBOOK,logbookEntries);
    }

    static public class logBookEntry implements Bundlable {
        @Packable
        public String text;

        @Packable
        public int color;

        //for restoreFromBundle
        public logBookEntry(){}

        logBookEntry(String text, int color) {
            this.text = text;
            this.color = color;
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {

        }

        @Override
        public void storeInBundle(Bundle bundle) {

        }

        @Override
        public boolean dontPack() {
            return false;
        }
    }
}
