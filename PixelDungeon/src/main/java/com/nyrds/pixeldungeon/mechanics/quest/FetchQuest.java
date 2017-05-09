package com.nyrds.pixeldungeon.mechanics.quest;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Item;

/**
 * Created by DeadDie on 09.05.2017
 */
public class FetchQuest extends Quest {

    private static Item questItem = null;

    private void setQuestItem(Item item) {
        questItem = item;
    }

    @Override
    protected boolean checkForCompletion(){
        Item item = Dungeon.hero.belongings.getItem(questItem.getClass());
        if ( item != null){
            item.removeItemFrom(Dungeon.hero);
            return true;
        } else{
            return false;
        }

    }
}
