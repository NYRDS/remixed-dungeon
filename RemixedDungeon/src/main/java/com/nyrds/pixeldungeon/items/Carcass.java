package com.nyrds.pixeldungeon.items;

import androidx.annotation.Keep;

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.effects.Devour;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.CharsList;
import com.nyrds.platform.util.StringsManager;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.ResultDescriptions;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.Doom;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.effects.Wound;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.plants.Sungrass;
import com.watabou.pixeldungeon.sprites.DummySprite;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import lombok.val;

public class Carcass extends Item implements Doom {
    public static final String AC_NECROMANCY = "AC_Necromancy";
    public static final String AC_DEVOUR = "AC_Devour";
    public static final String AC_KICK = "AC_Kick";
    public static final String CARCASS_OF = "Carcass of ";
    public static final String CARCASS = "Carcass";
    @Packable
    Char src = CharsList.DUMMY;

    @Packable
    int ttl = 10;

    static final int MAX_TTL = 10;

    {
        stackable = true;
    }

    @Keep
    public Carcass() {
    }

    public Carcass(Char src) {
        this.src = src;
        Actor.add(this);
        spend(10);
    }

    @Override
    protected boolean act() {
        spend(1);
        if(getOwner().valid()) {
            ttl = MAX_TTL;
            return true;
        }
        ttl--;
        if(ttl <= 0) {
            val heap = getHeap();
            if(heap!= null) {
                heap.replace(this, null);
                heap.updateHeap();
                CellEmitter.center(heap.pos).burst(Speck.factory(Speck.EVOKE), 3);
            }

        }
        return true;
    }

    @Override
    public Image getCustomImage() {
        if(src.valid()) {
            return src.newSprite().carcass();
        } else {
            return DummySprite.instance;
        }

    }

    @Override
    public String name() {
        return Utils.format(R.string.Carcass_Name, src.getName());
    }

    @Override
    public String desc() {
        return Utils.format(R.string.Carcass_Info, src.getName());
    }

    @Override
    public ArrayList<String> actions(Char hero) {
        var actions = super.actions(hero);
        if(getHeap()!= null) {
            actions.add(AC_KICK);
        }

        switch (hero.getHeroClass()) {
            case NECROMANCER:
                actions.add(AC_NECROMANCY);
            break;
            case GNOLL:
                if(hero.hunger().isHungry()) {
                    actions.add(AC_DEVOUR);
                }
            break;
        }

        return actions;
    }

    @Override
    public void _execute(@NotNull Char chr, @NotNull String action){
        if(action.equals(AC_KICK)) {
            GLog.i(Utils.format(R.string.Carcass_Kick, src.getName()));
            removeItem();
        }

        if (action.equals(AC_NECROMANCY)) {
            Level level = chr.level();
            int casterPos = chr.getPos();
            int spawnPos = level.getEmptyCellNextTo(casterPos);

            Wound.hit(chr);
            chr.damage(src.ht()/4, this);
            Buff.detach(chr, Sungrass.Health.class);

            if (level.cellValid(spawnPos)) {
                var pet = Mob.makePet((Mob) src, chr.getId());
                pet.regenSprite();
                pet.assigndNextId();
                pet.setUndead(true);
                pet.hp(1); //it's alive!
                pet.heal(pet.ht() * chr.skillLevel() / 10);
                pet.setPos(spawnPos);
                level.spawnMob(pet, 0, casterPos);
                GLog.p(Utils.format(R.string.Carcass_Necromancy, pet.getName()));
            } else {
                GLog.n(Utils.format(R.string.Carcass_Necromancy_Failed, src.getName()));
            }
            removeItem();

        } else if (action.equals(AC_DEVOUR)) {
            Devour.hit(chr);
            chr.eat(this, src.ht(), Utils.format(R.string.Carcass_Devoured, src.getName()));
            chr.heal(src.ht()/10, this);

            removeItem();
        } else {
            super._execute(chr, action);
        }
    }

    @Override
    public void onHeroDeath() {

        Dungeon.fail( Utils.format( ResultDescriptions.getDescription(ResultDescriptions.Reason.NECROMANCY), Dungeon.depth ) );
        GLog.n(StringsManager.getVar(R.string.Necromancy_Death));
    }

    @Override
    public String getEntityKind() {
        if (src != null && src.valid()) {
            return CARCASS_OF + src.getEntityKind();
        } else {
            return CARCASS;
        }
    }
}
