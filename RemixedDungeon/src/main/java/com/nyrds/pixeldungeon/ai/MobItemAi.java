package com.nyrds.pixeldungeon.ai;

import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.utils.GLog;

import java.util.ArrayList;

public class MobItemAi {

    public enum Context {
        COMBAT,
        FLEEING,
        IDLE
    }

    // Score threshold — below this, don't use the item.
    private static final float USE_THRESHOLD = 0.1f;

    // Action string constants (match the private/protected fields on Item/Wand).
    private static final String AC_THROW = "Item_ACThrow";
    private static final String AC_ZAP   = "Wand_ACZap";

    /**
     * Attempt to use an item from the mob's belongings.
     * Returns true if an item was used (turn consumed), false otherwise.
     */
    public static boolean tryUseItem(Mob mob, Context context) {
        if (!mob.isHumanoid()) {
            return false;
        }

        Item bestItem = null;
        String bestAction = null;
        float bestScore = 0;

        for (Item item : mob.getBelongings()) {
            ArrayList<String> actions = item.actions(mob);
            for (String action : actions) {
                float score = scoreItemAction(mob, item, action, context);
                if (score > bestScore) {
                    bestScore = score;
                    bestItem = item;
                    bestAction = action;
                }
            }
        }

        if (bestItem != null && bestScore > USE_THRESHOLD) {
            GLog.debug("MobItemAi: %s uses %s (%s) score %.2f",
                    mob.getEntityKind(), bestItem.getEntityKind(), bestAction, bestScore);
            bestItem.execute(mob, bestAction);
            return true;
        }

        return false;
    }

    private static float scoreItemAction(Mob mob, Item item, String action, Context context) {
        // Placeholder — implemented in Task 3.
        return 0;
    }
}
