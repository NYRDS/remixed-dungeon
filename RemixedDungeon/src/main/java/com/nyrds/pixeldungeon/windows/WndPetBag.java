package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.items.Item;
import com.nyrds.pixeldungeon.actors.hero.Hero;
import com.nyrds.pixeldungeon.actors.mobs.Mob;
import com.nyrds.pixeldungeon.mechanics.PetInventoryManager;
import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.utils.GLog;
import com.nyrds.pixeldungeon.utils.Utils;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.items.bags.Bag;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.windows.elements.Tab;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class WndPetBag extends WndBag {

    private final Hero hero;
    private final Mob pet;
    private final Text txtPetName;
    private final Text txtPetHP;

    public WndPetBag(@NotNull Hero hero, @NotNull Mob pet) {
        super(pet.getBelongings(), pet.getBelongings().backpack, new PetBagListener(hero, pet), Mode.ALL, pet.getName());

        this.hero = hero;
        this.pet = pet;

        // Update title to show pet's name and HP
        updatePetInfo();

        // Add a close button if needed
        // The window already has back/menu handling from WndTabbed
    }

    private void updatePetInfo() {
        if (txtTitle != null) {
            txtTitle.text(Utils.capitalize(pet.getName()) + " " + Utils.format(R.string.WndPetBag_HP, pet.hp(), pet.ht()));
            txtTitle.setX(PixelScene.align((panelWidth - txtTitle.width()) / 2));
        }
    }

    @Override
    public void onClick(Tab tab) {
        super.onClick(tab);
        updatePetInfo();
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
        if (listener != null) {
            listener.onSelect(null, hero);
        }
        super.onBackPressed();
    }

    private static class PetBagListener implements Listener {
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