package com.nyrds.pixeldungeon.effects;

import com.nyrds.util.JsonHelper;
import com.nyrds.util.ModError;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class EffectsFactory {

    private static final Map<String, JSONObject> effects = new HashMap<>();

    static {
        for(String effectFile: ModdingMode.listResources("effects", (dir, name) -> name.endsWith(".json"))) {
            effects.put(effectFile.replace(".json", Utils.EMPTY_STRING),JsonHelper.readJsonFromAsset("effects/"+effectFile));
        }
    }

    public static CustomClipEffect getEffectByName(String name) {

        CustomClipEffect effect = new CustomClipEffect();
        try {
            effect.setupFromJson(effects.get(name));
        } catch (JSONException e) {
            throw new ModError(name, e);
        }

        return effect;
    }

    public static boolean isValidEffectName(String zapEffect) {
        return effects.containsKey(zapEffect);
    }
}
