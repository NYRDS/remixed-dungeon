package com.nyrds.pixeldungeon.support;

import com.nyrds.LuaInterface;

@LuaInterface
public class AdsInterstitial {

    @LuaInterface
    static public void show() {
        AdsUtilsCommon.showInterstitial(result -> { });
    }
}
