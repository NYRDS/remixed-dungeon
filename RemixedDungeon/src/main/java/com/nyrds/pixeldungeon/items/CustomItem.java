package com.nyrds.pixeldungeon.items;

import com.nyrds.Packable;
import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.pixeldungeon.mechanics.LuaScript;
import com.watabou.noosa.StringsManager;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.scenes.CellSelector;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.utils.Bundle;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;

import java.util.ArrayList;

import androidx.annotation.Keep;

/**
 * Created by mike on 26.05.2018.
 * This file is part of Remixed Pixel Dungeon.
 */
public class CustomItem extends Item {

    @Packable
    private String scriptFile;

    private boolean upgradable;
    private boolean identified;

    private LuaScript script;
    private int price;

    @Keep
    public CustomItem() {
    }

    public CustomItem(String scriptFile) {
        this.scriptFile = scriptFile;
        initItem();
    }

    private void initItem() {
        script = new LuaScript("scripts/items/"+scriptFile, this);

        script.run("itemDesc",null,null);
        LuaTable desc = script.getResult().checktable();

        image        = desc.rawget("image").checkint();
        imageFile    = desc.rawget("imageFile").checkjstring();
        name         = StringsManager.maybeId(desc.rawget("name").checkjstring());
        info         = StringsManager.maybeId(desc.rawget("info").checkjstring());
        stackable    = desc.rawget("stackable").checkboolean();
        upgradable    = desc.rawget("upgradable").checkboolean();
        identified    = desc.rawget("identified").checkboolean();
        setDefaultAction(desc.rawget("defaultAction").checkjstring());
        price         = desc.rawget("price").checkint();
    }

    @Override
    public boolean isUpgradable() {
        return upgradable;
    }

    @Override
    public boolean isIdentified() {
        return identified;
    }

    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero,action);
        script.run("execute", hero, action);
    }

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);

        script.run("actions", hero, null);

        if(script.getResult().istable()) {
            LuaTable luaActions = script.getResult().checktable();

            int n = luaActions.rawlen();

            for(int i =1;i<=n;++i){
                actions.add(luaActions.rawget(i).checkjstring());
            }

        }
        return actions;
    }

    public void selectCell(String action,String prompt) {
        CellSelector.Listener cellSelectorListener= new CellSelector.Listener(){

            @Override
            public void onSelect(Integer cell) {
                script.run("cellSelected", action, cell);
            }

            @Override
            public String prompt() {
                return StringsManager.maybeId(prompt);
            }
        };

        GameScene.selectCell(cellSelectorListener);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        initItem();
    }

    private Item applyOnItem(int cell,String effect) {
        try {
            script.run(effect, cell, null);
            if(script.getResult().isnil()) {
                return null;
            }

            Item item = (Item) script.getResult().checkuserdata(Item.class);
            item.quantity(quantity());
            return item;
        } catch (LuaError e) {
            throw new TrackedRuntimeException(e);
        }
    }


    @Override
    public Item burn(int cell) {
        return applyOnItem(cell,"burn");
    }

    @Override
    public Item freeze(int cell) {
        return applyOnItem(cell,"freeze");
    }

    @Override
    public Item poison(int cell) {
        return applyOnItem(cell,"poison");
    }

    @Override
    protected void onThrow(int cell) {
        script.run("onThrow", cell, null);
    }

    @Override
    public String getClassName() {
        return scriptFile;
    }

    @Override
    public int price() {
        return price * quantity();
    }
}
