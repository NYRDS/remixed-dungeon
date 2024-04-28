package com.nyrds.pixeldungeon.items;

import androidx.annotation.Keep;

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.Wound;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.plants.Sungrass;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class Carcass extends Item {
    @Packable
    Char owner;

    @Keep
    public Carcass() {
    }
    public Carcass(Char owner) {
        this.owner = owner;
    }

    @Override
    public Image getCustomImage() {
        return owner.getSprite().carcass();
    }

    @Override
    public String name() {
        return Utils.format(R.string.Carcass_Name, owner.getName_objective());
    }

    @Override
    public String desc() {
        return Utils.format(R.string.Carcass_Info, owner.getName_objective());
    }

    @Override
    public ArrayList<String> actions(Char hero) {
        var actions = new ArrayList<String>();

        if (hero.getHeroClass() == HeroClass.NECROMANCER) {
            actions.add("Necromancy");
        }

        return actions;
    }

    @Override
    public void execute(@NotNull Char chr, @NotNull String action){
        if (action.equals("Necromancy")) {
            Level level = chr.level();
            int casterPos = chr.getPos();
            int spawnPos = level.getEmptyCellNextTo(casterPos);

            Wound.hit(chr);
            Buff.detach(chr, Sungrass.Health.class);

            if (level.cellValid(spawnPos)) {
                var pet = Mob.makePet((Mob) owner, chr.getId());
                pet.hp(1); //it's alive!
                pet.regenSprite();
                pet.heal(pet.ht() * chr.skillLevel() / 10);
                pet.setGlowing(0xff333333, 5f);

                pet.setPos(spawnPos);
                level.spawnMob(pet, 0, casterPos);
            }
        }
    }
}
