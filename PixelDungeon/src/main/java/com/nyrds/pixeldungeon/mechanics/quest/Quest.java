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


    void completeQuest(){
        completed = true;
    }

    void startQuest(){
        if (!started){
            started = true;
        }
    }

    void turnInQuest(){
        turnedIn = true;
    }

    public boolean progressQuest(boolean questStage){
        if (!questStage){
            return true;
        } else {
            return false;
        }
    }

    public static void reset() {
        started = false;
        completed = false;
        turnedIn = false;
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
