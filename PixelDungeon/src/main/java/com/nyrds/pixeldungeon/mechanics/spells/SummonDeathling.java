package com.nyrds.pixeldungeon.mechanics.spells;

import com.nyrds.pixeldungeon.mechanics.Necromancy;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.common.Deathling;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Game;
import com.watabou.noosa.TextureFilm;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.Wound;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.plants.Sungrass;
import com.watabou.pixeldungeon.utils.GLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;

public class SummonDeathling extends SummoningSpell {

    public SummonDeathling(){
        targetingType = SpellHelper.TARGET_NONE;
        magicAffinity = SpellHelper.AFFINITIES[0];
        name          = Game.getVar(R.string.Necromancy_SummonDeathlingName);
        desc          = Game.getVar(R.string.SummonDeathling_Info);
        imageIndex = 0;
    }

    @Override
    public void cast(Hero hero){
        super.cast(hero);
            Collection<Mob> pets = Dungeon.hero.getPets();

            int n = 0;
            for (Mob mob : pets){
                if (mob.isAlive() && mob instanceof Deathling) {
                    n++;
                }
            }

            int spawnPos = Dungeon.level.getEmptyCellNextTo(hero.getPos());

            Wound.hit(hero);
            Buff.detach(hero, Sungrass.Health.class);

            if (Dungeon.level.cellValid(spawnPos)) {
                Mob pet = Mob.makePet(new Deathling(), hero);
                pet.setPos(spawnPos);
                Dungeon.level.spawnMob(pet);
            }

            hero.spend(1/hero.speed());
    }

    @Override
    public String texture(){
        return "spellsIcons/necromancy.png";
    }

    @Override
    public int textureResolution(){
        return 32;
    }
}
