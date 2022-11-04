package com.nyrds.pixeldungeon.effects;

import org.json.JSONException;
import org.json.JSONObject;

public interface ICustomEffect {
    void place(int cell);
    void playAnimOnce();
    void setupFromJson(JSONObject json) throws JSONException;
}
