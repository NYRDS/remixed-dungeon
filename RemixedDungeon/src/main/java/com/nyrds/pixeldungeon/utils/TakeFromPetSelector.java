package com.nyrds.pixeldungeon.utils;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.windows.WndBag;
import com.nyrds.pixeldungeon.windows.WndPetQuantity;

public class TakeFromPetSelector implements WndBag.Listener {
    private final Mob pet;

    public TakeFromPetSelector(Mob pet) {
        this.pet = pet;
    }

    @Override
    public void onSelect(Item item, Char selector) {
        if (item != null && item.valid()) {
            // Show quantity selector for stackable items
            if (item.quantity() > 1) {
                GameScene.show(new WndPetQuantity(item, (Hero) selector, pet));
            } else {
                // Transfer single item directly
                com.nyrds.pixeldungeon.mechanics.PetInventoryManager.takeItemFromPet(
                    (Hero) selector, pet, item);
            }
        }
    }
}