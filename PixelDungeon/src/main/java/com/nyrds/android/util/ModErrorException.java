package com.nyrds.android.util;

import com.watabou.noosa.Game;

public class ModErrorException extends TrackedRuntimeException {

    public ModErrorException(String s, Exception e) {
        super(s, e);
        Game.toast("%s -> %s",s, e.getMessage());
    }
}
