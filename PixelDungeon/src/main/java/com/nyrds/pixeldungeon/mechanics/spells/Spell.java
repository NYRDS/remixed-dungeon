package com.nyrds.pixeldungeon.mechanics.spells;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Game;
import com.watabou.noosa.TextureFilm;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class Spell {
    private static final String TXT_NOT_ENOUGH_SOULS   = Game.getVar(R.string.Necromancy_NotEnoughSouls);

    protected String targetingType;
    protected String magicAffinity;

    protected String textureFile = "spells/common.png";
    protected String name = "Spell";
    protected int textureResolution = 16;
    protected int imageIndex = 0;
    private int spellCost = 5;

    private SmartTexture icon = TextureCache.get(textureFile);
    private TextureFilm film = new TextureFilm( icon, textureResolution, textureResolution );

    void setupFromJson( JSONObject obj) throws JSONException {
        name = obj.optString("name", name);
        textureFile = obj.optString("textureFile", textureFile);
        imageIndex  = obj.optInt("imageIndex", imageIndex);
        magicAffinity = obj.optString("magicAffinity", magicAffinity);
        targetingType = obj.optString("targetingType", targetingType);
        textureResolution  = obj.optInt("textureResolution", textureResolution);
        spellCost  = obj.optInt("spellCost", spellCost);
    }

    public void cast(Hero hero){
        if(!hero.spendSoulPoints(spellCost())){
            GLog.w( notEnoughSouls(name) );
            return;
        }

    }

    public String getMagicAffinity(){return magicAffinity;}

    public String texture(){
        return textureFile;
    }

    public int image() {
        return imageIndex;
    }

    public int spellCost(){
      return spellCost;
    }

    public static String notEnoughSouls (String spell) {
        return Utils.format(TXT_NOT_ENOUGH_SOULS, spell);
    }
}
