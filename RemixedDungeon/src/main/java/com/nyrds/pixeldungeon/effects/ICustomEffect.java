package com.nyrds.pixeldungeon.effects;

import clone.org.json.JSONException;
import clone.org.json.JSONObject;

public interface ICustomEffect {
    void place(int cell);
    void playAnimOnce();
    void setupFromJson(JSONObject json) throws JSONException;
}
