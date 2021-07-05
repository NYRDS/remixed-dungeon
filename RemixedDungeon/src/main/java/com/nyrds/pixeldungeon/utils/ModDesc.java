package com.nyrds.pixeldungeon.utils;

import com.nyrds.platform.game.RemixedDungeon;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

public class ModDesc {
    public String  url         = Utils.EMPTY_STRING;
    public String  name        = Utils.EMPTY_STRING;
    public String  author      = Utils.EMPTY_STRING;
    public String  hrVersion   = Utils.EMPTY_STRING;
    public String  description = Utils.EMPTY_STRING;
    public String  installDir  = Utils.EMPTY_STRING;
    public int     version;
    public int     rpdVersion;
    public boolean needUpdate = false;
    public boolean installed  = false;

    public static void fromJson(@NotNull ModDesc ret, @NotNull JSONObject modVersion) throws JSONException {
        ret.version   = modVersion.getInt("version");
        ret.author    = modVersion.optString("author", "Unknown");
        ret.description = modVersion.optString("description", "");
        ret.name      = modVersion.optString("name", ret.installDir);
        ret.url       = modVersion.optString("url", "");
        ret.hrVersion = modVersion.optString("hr_version", String.valueOf(ret.version));
        ret.rpdVersion = modVersion.optInt("rpd_version", 0);
    }

    public boolean isCompatible() {
        return rpdVersion <= (RemixedDungeon.versionCode % 2000);
    }
}
