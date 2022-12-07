package com.nyrds.retrodungeon.mechanics.spells;

import android.support.annotation.NonNull;

import com.nyrds.retrodungeon.ml.R;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.scenes.CellSelector;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class Spell {
    private static final String TXT_NOT_ENOUGH_SOULS   = Game.getVar(R.string.Spells_NotEnoughSP);
    private static final String TXT_SELECT_CELL   = Game.getVar(R.string.Spell_SelectACell);

    protected int level = 1;
    protected int spellCost = 5;
    protected int textureResolution = 32;

    protected float castTime = 1f;
    protected float duration = 10f;

    protected String targetingType;
    protected String magicAffinity;

    protected String textureFile = "spellsIcons/common.png";

    protected String name = getClassParam("Name", Game.getVar(R.string.Item_Name), false);
    protected String desc = getClassParam("Info", Game.getVar(R.string.Item_Info), false);

    private SmartTexture icon = TextureCache.get(texture());

    public int imageIndex = 0;
    public TextureFilm film = new TextureFilm( icon, textureResolution(), textureResolution() );

    void setupFromJson( JSONObject obj) throws JSONException {
        name = obj.optString("name", name);
        desc = obj.optString("name", desc);
        textureFile = obj.optString("textureFile", textureFile);
        imageIndex  = obj.optInt("imageIndex", imageIndex);
        magicAffinity = obj.optString("magicAffinity", getMagicAffinity());
        targetingType = obj.optString("targetingType", targetingType);
        textureResolution  = obj.optInt("textureResolution", textureResolution());
        spellCost  = obj.optInt("spellCost", spellCost());
        duration  = obj.optInt("duration", (int)duration);
    }

	protected boolean cast(Char chr, int cell) {
		return true;
	}

    public boolean cast(@NonNull final Char chr){

        if(!chr.isAlive()) {
            return false;
        }

	    if(chr instanceof Hero) {
		    final Hero hero = (Hero)chr;

            if (!hero.enoughSP(spellCost())) {
                GLog.w(notEnoughSouls(name));
                return false;
            }

            if (targetingType.equals(SpellHelper.TARGET_CELL)) {
                GameScene.selectCell(new CellSelector.Listener() {
                    @Override
                    public void onSelect(Integer cell) {
                        if (cell != null){
                            cast(chr, cell);

                            hero.spend(castTime);
                            hero.busy();
                            hero.getSprite().zap(hero.getPos());
                        }
                    }

                    @Override
                    public String prompt() {
                        return TXT_SELECT_CELL;
                    }
                });
                return false;
            }
        }
		return true;
    }

    public void castCallback(Char chr){
    	if(chr instanceof Hero) {
		    ((Hero)chr).spendSoulPoints(spellCost());
	    }
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

    protected String getClassParam(String paramName, String defaultValue, boolean warnIfAbsent) {
        return Utils.getClassParam(this.getClass().getSimpleName(), paramName, defaultValue, warnIfAbsent);
    }

    public int level(){
        return level;
    }

    public int getLevelModifier(Char chr){
        return chr.magicLvl() - level;
    }
}