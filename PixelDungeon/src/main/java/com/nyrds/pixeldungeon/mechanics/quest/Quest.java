package com.nyrds.pixeldungeon.mechanics.quest;

import com.watabou.pixeldungeon.items.Item;

/**
 * Created by DeadDie on 09.05.2017
 */
public class Quest {

    private static boolean started = false;
    private static boolean completed = false;
    private static boolean turnedIn = false;
    private static int questProgress = 0;
    protected static int questQuantity = 1;

    public Quest(){
    }

    void startQuest(){ started = true; }

    void completeQuest(){
        completed = true;
    }

    void turnInQuest(){
        turnedIn = true;
    }

    public boolean isStarted(){
        return started;
    }

    public boolean isCompleted(){
        return completed;
    }

    public boolean isTurnedIn(){
        return turnedIn;
    }

    public static void reset() {
        started = false;
        completed = false;
        turnedIn = false;
        questProgress = 0;
        questQuantity = 1;
    }

    protected void checkForCompletion(){
        if (questProgress >= questQuantity) {
            completeQuest();
        }
    }

    private void progressQuest(){
        questQuantity++;
    }

}
