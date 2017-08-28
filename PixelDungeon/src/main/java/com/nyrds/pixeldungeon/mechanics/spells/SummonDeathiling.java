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

public class SummonDeathiling extends Spell {

    private int limit = 1;

    SummonDeathiling(){
       targetingType = SpellHelper.TARGET_NONE;
       magicAffinity = SpellHelper.AFFINITIES[1];
       name          = Game.getVar(R.string.Necromancy_SummonDeathlingName);
    }

    protected String textureFile = "spells/common.png";
    protected int textureResolution = 16;
    protected int imageIndex = 0;

    private SmartTexture icon = TextureCache.get(textureFile);
    private TextureFilm film = new TextureFilm( icon, textureResolution, textureResolution );


    public void cast(){
            Collection<Mob> pets = Dungeon.hero.getPets();
            Hero hero = Dungeon.hero;

            int n = 0;
            for (Mob mob : pets){
                if (mob.isAlive() && mob instanceof Deathling) {
                    n++;
                }
            }

            if (n >= limit){
                GLog.w( Necromancy.getLimitWarning(limit) );
                return;
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

    public String getMagicAffinity(){return magicAffinity;}

    public String texture(){
        return textureFile;
    }

    public int image() {
        return imageIndex;
    }
}
