package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.mechanics.PetInventoryManager;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.actors.hero.Hero;
import com.nyrds.pixeldungeon.actors.mobs.Mob;
import com.nyrds.pixeldungeon.scenes.GameScene;
import com.nyrds.pixeldungeon.windows.HBox;
import com.nyrds.pixeldungeon.windows.VHBox;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WndPetSelect extends Window {

    private static final float BUTTON_WIDTH = 36;
    private VHBox actions;

    private final Hero hero;
    private final List<Mob> pets;

    public WndPetSelect(@NotNull Hero hero) {

        super();

        int WIDTH = stdWidth();
        this.hero = hero;
        this.pets = PetInventoryManager.getHeroPets(hero);

        Text title = PixelScene.createMultiline(StringsManager.getVar(R.string.WndPetSelect_Title), GuiProperties.titleFontSize());
        title.maxWidth(WIDTH);
        title.setX(PixelScene.align((WIDTH - title.width()) / 2));
        title.setY(0);
        add(title);

        float y = title.bottom() + GAP;

        actions = new VHBox(WIDTH);
        actions.setAlign(HBox.Align.Width);
        actions.setGap(GAP);

        for (final Mob pet : pets) {
            RedButton btn = new RedButton(getPetDisplayName(pet)) {
                @Override
                protected void onClick() {
                    hide();
                    GameScene.show(new WndPetBag(hero, pet));
                }
            };
            btn.setSize(Math.max(BUTTON_WIDTH, btn.reqWidth()), BUTTON_HEIGHT);
            actions.add(btn);
        }

        add(actions);
        actions.setPos(PixelScene.align((WIDTH - actions.width()) / 2), y);

        resize(WIDTH, (int) (actions.bottom() + GAP));
    }

    private String getPetDisplayName(@NotNull Mob pet) {
        String name = pet.getName();
        String hp = Utils.format(R.string.WndPetSelect_HP, pet.hp(), pet.ht());
        return name + " " + hp;
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
        hide();
        super.onBackPressed();
    }
}