package com.nyrds.pixeldungeon.utils;

import com.nyrds.pixeldungeon.mechanics.PetInventoryManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.windows.WndBag;
import com.nyrds.pixeldungeon.windows.WndPetQuantity;

public class TakeFromPetSelector implements WndBag.Listener {
    private final Hero hero;
    private final Mob pet;

    public TakeFromPetSelector(Hero hero, Mob pet) {
        this.hero = hero;
        this.pet = pet;
    }

    @Override
    public void onSelect(Item item, Char selector) {
        if (item != null && item.valid()) {
            // NOTE: `selector` here is item.getOwner() (see ItemButton.onClick), i.e. the PET whose
            // bag is being viewed — NOT the hero. The hero (transfer destination) is held explicitly.
            // Casting `selector` to Hero caused a ClassCastException for pet-owned items.
            // Show quantity selector for stackable items
            if (item.quantity() > 1) {
                GameScene.show(new WndPetQuantity(item, hero, pet));
            } else {
                // Transfer single item directly
                PetInventoryManager.takeItemFromPet(hero, pet, item);
            }
        }
    }
}
