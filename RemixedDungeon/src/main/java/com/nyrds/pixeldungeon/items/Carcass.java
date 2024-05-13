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
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.Doom;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.Wound;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.plants.Sungrass;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import lombok.val;

public class Carcass extends Item implements Doom {
    @Packable
    Char src = CharsList.DUMMY;

    @Packable
    int ttl = 10;

    static final int MAX_TTL = 10;

    @Keep
    public Carcass() {
    }
    public Carcass(Char src) {
        this.src = src;
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
            }

        }
        return true;
    }

    @Override
    public Image getCustomImage() {
        return src.getSprite().carcass();
    }

    @Override
    public String name() {
        return Utils.format(R.string.Carcass_Name, src.getName_objective());
    }

    @Override
    public String desc() {
        return Utils.format(R.string.Carcass_Info, src.getName_objective());
    }

    @Override
    public ArrayList<String> actions(Char hero) {
        var actions = super.actions(hero);

        switch (hero.getHeroClass()) {
            case NECROMANCER:
                actions.add("Necromancy");
            break;
            case GNOLL:
                actions.add("Devour");
            break;
        }

        return actions;
    }

    @Override
    public void _execute(@NotNull Char chr, @NotNull String action){
        if (action.equals("Necromancy")) {
            Level level = chr.level();
            int casterPos = chr.getPos();
            int spawnPos = level.getEmptyCellNextTo(casterPos);

            Wound.hit(chr);
            chr.damage(src.ht()/5, this);
            Buff.detach(chr, Sungrass.Health.class);

            if (level.cellValid(spawnPos)) {
                var pet = Mob.makePet((Mob) src, chr.getId());
                pet.hp(1); //it's alive!
                pet.regenSprite();
                pet.heal(pet.ht() * chr.skillLevel() / 10);
                pet.setGlowing(0xff333333, 5f);

                pet.setPos(spawnPos);
                level.spawnMob(pet, 0, casterPos);
            }
            chr.getBelongings().removeItem(this);
        } else if (action.equals("Devour")) {
            Devour.hit(chr);
            chr.eat(this, src.ht(), "You have devoured corpse of " + src.getName() + "!");
            chr.heal(src.ht()/10, this);

            chr.getBelongings().removeItem(this);
        } else {
            super._execute(chr, action);
        }
    }

    @Override
    public void onHeroDeath() {
        Dungeon.fail( Utils.format( ResultDescriptions.getDescription(ResultDescriptions.Reason.BURNING), Dungeon.depth ) );
        GLog.n(StringsManager.getVar(R.string.Burning_Death));
    }

    @Override
    public String getEntityKind() {
        if (src != null && src.valid()) {
            return "Carcass of " + src.getName();
        } else {
            return "Carcass";
        }
    }
}
