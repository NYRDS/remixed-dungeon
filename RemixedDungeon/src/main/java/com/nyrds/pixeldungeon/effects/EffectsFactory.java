package com.nyrds.pixeldungeon.effects;

import com.nyrds.android.util.JsonHelper;
import com.nyrds.android.util.ModError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class EffectsFactory {

    static Map<String, JSONObject> effects = new HashMap<>();

    public static CustomClipEffect getEffectByName(String name) {
        String descFileName = "effects/"+name+".json";

        JSONObject effectDesc;
        if(effects.containsKey(descFileName)) {
            effectDesc = effects.get(descFileName);
        } else {
            effectDesc = JsonHelper.readJsonFromAsset(descFileName);
            effects.put(descFileName,effectDesc);
        }

        CustomClipEffect effect = new CustomClipEffect();
        try {
            effect.setupFromJson(effectDesc);
        } catch (JSONException e) {
            throw new ModError(descFileName, e);
        }

        return effect;
    }
}
