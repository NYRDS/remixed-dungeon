package com.nyrds.platform.support;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.support.AdsUtilsCommon;

@LuaInterface
public class AdsInterstitial {

    @LuaInterface
    static public void show() {
        AdsUtilsCommon.showInterstitial(result -> { });
    }
}
