package com.nyrds.pixeldungeon.mechanics.spells;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class Spell {
    private static final String TXT_NOT_ENOUGH_SOULS   = Game.getVar(R.string.Necromancy_NotEnoughSouls);
    private int spellCost = 5;

    protected String targetingType;
    protected String magicAffinity;

    protected String textureFile = "spellsIcons/common.png";
    protected String name = "Spell";
    protected String desc = "Description";
    protected int textureResolution = 16;

    private SmartTexture icon = TextureCache.get(texture());

    public int imageIndex = 0;
    public TextureFilm film = new TextureFilm( icon, textureResolution(), textureResolution() );

    void setupFromJson( JSONObject obj) throws JSONException {
        name = obj.optString("name", name);
        desc = obj.optString("name", desc);
        textureFile = obj.optString("textureFile", textureFile);
        imageIndex  = obj.optInt("imageIndex", imageIndex);
        magicAffinity = obj.optString("magicAffinity", magicAffinity);
        targetingType = obj.optString("targetingType", targetingType);
        textureResolution  = obj.optInt("textureResolution", textureResolution());
        spellCost  = obj.optInt("spellCost", spellCost);
    }

    public void use(Hero hero){
        if(!hero.spendSoulPoints(spellCost())){
            GLog.w( notEnoughSouls(name) );
            return;
        }
        cast(hero);
    }

    protected void cast(Hero hero){

    }

    public String name(){
        return name;
    }

    public String desc(){
        return desc;
    }

    public String getMagicAffinity(){return magicAffinity;}

    public String texture(){
        return textureFile;
    }

    public Image image() {
        return new Image( icon() );
    }

    public int spellCost(){
      return spellCost;
    }

    public static String notEnoughSouls (String spell) {
        return Utils.format(TXT_NOT_ENOUGH_SOULS, spell);
    }

    public SmartTexture icon (){
        return icon;
    }

    public int textureResolution(){
        return textureResolution;
    }
}