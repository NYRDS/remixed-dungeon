package com.watabou.pixeldungeon.plants;

import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.effects.SpellSprite;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.bags.SeedPouch;
import com.watabou.pixeldungeon.items.food.Food;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import lombok.SneakyThrows;

public class Seed extends Item {

    public static final String AC_PLANT = "Plant_ACPlant";

    private static final float TIME_TO_PLANT = 1f;

    {
        stackable = true;
        setDefaultAction(AC_THROW);
        imageFile = "items/seeds.png";
    }

    protected Class<? extends Plant> plantClass;
    protected String plantName;

    public Class<? extends Item> alchemyClass;

    @Override
    public ArrayList<String> actions(Char hero) {
        ArrayList<String> actions = super.actions(hero);
        actions.add(AC_PLANT);
        actions.add(CommonActions.AC_EAT);
        return actions;
    }

    @Override
    protected void onThrow(int cell, @NotNull Char thrower) {
        Level level = thrower.level();

        if (level.pit[cell] || level.getTopLevelObject(cell) != null) {
            super.onThrow(cell, thrower);
        } else {
            level.plant(this, cell);
        }
    }

    @Override
    public void _execute(@NotNull Char chr, @NotNull String action) {
        if (action.equals(AC_PLANT)) {
            ((Seed) detach(chr.getBelongings().backpack)).onThrow(chr.getPos(), chr);
            chr.doOperate(TIME_TO_PLANT);

        } else if (action.equals(CommonActions.AC_EAT)) {
            detach(chr.getBelongings().backpack);

            chr.doOperate(Food.TIME_TO_EAT);

            SpellSprite.show(chr, SpellSprite.FOOD);
            Sample.INSTANCE.play(Assets.SND_EAT);
        }

        super._execute(chr, action);
    }


    @Override
    public Item burn(int cell){
        return null;
    }

    @SneakyThrows
    public Plant couch(int pos) {
        Sample.INSTANCE.play(Assets.SND_PLANT);
        Plant plant = plantClass.newInstance();
        plant.setPos(pos);
        return plant;
    }

    @Override
    public boolean isUpgradable() {
        return false;
    }

    @Override
    public boolean isIdentified() {
        return true;
    }

    @Override
    public int price() {
        return 10 * quantity();
    }

    @Override
    public String info() {
        return Utils.format(StringsManager.getVar(R.string.Plant_Info), Utils.indefinite(plantName), desc());
    }

    @Override
    public String bag() {
        return SeedPouch.class.getSimpleName();
    }
}
