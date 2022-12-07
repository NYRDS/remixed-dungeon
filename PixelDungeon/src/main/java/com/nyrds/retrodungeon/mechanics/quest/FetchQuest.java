package com.nyrds.retrodungeon.mechanics.quest;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.items.Item;

/**
 * Created by DeadDie on 09.05.2017
 */
public class FetchQuest extends Quest {

    private static Item questItem = null;

    private void setQuestItem(Item item, int quantity) {
        questItem = item;
        questQuantity = quantity;
    }

    private void setQuestItem(Item item) {
        setQuestItem(item, 1);
    }

    private boolean checkForItem(){
        Item item = Dungeon.hero.belongings.getItem(questItem.getClass());
        if ( item != null && item.quantity() >= questQuantity){
            item.removeItemFrom(Dungeon.hero);
            return true;
        } else{
            return false;
        }
    }

}
