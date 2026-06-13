package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.GiveToPetSelector;
import com.nyrds.pixeldungeon.utils.TakeFromPetSelector;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.bags.Bag;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndBag;
import com.watabou.pixeldungeon.windows.WndOptions;

public class WndPetInventoryOptions extends WndOptions {
    private final Char client;
    private final Bag heroBackpack;
    private final Bag petBackpack;
    private final Mob pet;

    public WndPetInventoryOptions(Hero hero, Mob pet) {
        super(Utils.capitalize(pet.getName()),
                StringsManager.getVar(R.string.PetInventory_Title),
                StringsManager.getVar(R.string.PetInventory_GiveToPet),
                StringsManager.getVar(R.string.PetInventory_TakeFromPet));
        this.client = hero;
        this.heroBackpack = hero.getBelongings().backpack;
        this.petBackpack = pet.getBelongings().backpack;
        this.pet = pet;
    }

    @Override
    public void onSelect(int index) {
        switch (index) {
            case 0:
                showGiveWnd();
                break;
            case 1:
                showTakeWnd();
                break;
        }
    }

    public void showGiveWnd() {
        GameScene.show(
            new WndBag(client.getBelongings(),
                        heroBackpack,
                        new GiveToPetSelector(pet),
                        WndBag.Mode.ALL,
                        StringsManager.getVar(R.string.PetInventory_GiveToPet)));
    }

    public void showTakeWnd() {
        GameScene.show(
            new WndBag(pet.getBelongings(),
                        petBackpack,
                        new TakeFromPetSelector(pet),
                        WndBag.Mode.ALL,
                        StringsManager.getVar(R.string.PetInventory_TakeFromPet)));
    }
}