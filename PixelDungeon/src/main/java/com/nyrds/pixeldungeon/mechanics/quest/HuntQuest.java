package com.nyrds.pixeldungeon.mechanics.quest;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Item;

/**
 * Created by DeadDie on 09.05.2017
 */
public class HuntQuest extends Quest {

    private static Mob questMob = null;

    private void setQuestTarget(Mob mob, int quantity) {
        questMob = mob;
        questQuantity = quantity;
    }

    private void setQuestTarget(Mob mob) {
        setQuestTarget(mob, 1);
    }

}
