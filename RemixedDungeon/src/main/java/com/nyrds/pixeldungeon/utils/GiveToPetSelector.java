package com.nyrds.pixeldungeon.utils;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.windows.WndBag;
import com.nyrds.pixeldungeon.windows.WndPetQuantity;

public class GiveToPetSelector implements WndBag.Listener {
    private final Hero hero;
    private final Mob pet;

    public GiveToPetSelector(Hero hero, Mob pet) {
        this.hero = hero;
        this.pet = pet;
    }

    @Override
    public void onSelect(Item item, Char selector) {
        if (item != null && item.valid()) {
            // `selector` is item.getOwner() (see ItemButton.onClick). It happens to be the hero
            // here because the give window shows the hero's bag, but don't rely on that — hold the
            // hero explicitly so this can't ClassCastException like TakeFromPetSelector did.
            // Show quantity selector for stackable items
            if (item.quantity() > 1) {
                GameScene.show(new WndPetQuantity(item, hero, pet));
            } else {
                // Transfer single item directly
                com.nyrds.pixeldungeon.mechanics.PetInventoryManager.giveItemToPet(hero, pet, item);
            }
        }
    }
}
