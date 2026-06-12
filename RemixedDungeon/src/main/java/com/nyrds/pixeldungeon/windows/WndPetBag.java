package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mechanics.PetInventoryManager;
import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.bags.Bag;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.windows.WndBag;
import com.watabou.pixeldungeon.windows.elements.Tab;

import org.jetbrains.annotations.NotNull;

public class WndPetBag extends WndBag {

    private final Hero hero;
    private final Mob pet;

    public WndPetBag(@NotNull Hero hero, @NotNull Mob pet) {
        super(pet.getBelongings(), pet.getBelongings().backpack, new PetBagListener(hero, pet), Mode.ALL,
              Utils.capitalize(pet.getName()) + " " + Utils.format(R.string.WndPetBag_HP, pet.hp(), pet.ht()));

        this.hero = hero;
        this.pet = pet;
    }

    @Override
    public void onClick(Tab tab) {
        super.onClick(tab);
        // Update title with current HP when switching tabs
        String title = Utils.capitalize(pet.getName()) + " " + Utils.format(R.string.WndPetBag_HP, pet.hp(), pet.ht());
        // Access the private txtTitle field via reflection is not ideal, but we can't modify parent class
        // For now, we'll just let the parent handle title updates
    }

    @Override
    public void onSignal(com.nyrds.platform.input.Keys.Key key) {
        if (key.pressed) {
            switch (key.code) {
                case android.view.KeyEvent.KEYCODE_I:
                case android.view.KeyEvent.KEYCODE_BACK:
                    hide();
                    break;
            }
        }
        super.onSignal(key);
    }

    @Override
    public void onBackPressed() {
        if (getListener() != null) {
            getListener().onSelect(null, hero);
        }
        super.onBackPressed();
    }

    private static class PetBagListener implements WndBag.Listener {
        private final Hero hero;
        private final Mob pet;

        public PetBagListener(Hero hero, Mob pet) {
            this.hero = hero;
            this.pet = pet;
        }

        @Override
        public void onSelect(Item item, Char selector) {
            if (item != null && item.valid()) {
                // Show WndItem with pet-specific actions
                GameScene.show(new WndPetItem(hero, pet, item));
            }
        }
    }
}