package com.nyrds.platform.app;


import com.nyrds.LuaInterface;

public class Input {

    static private String dialogInput = "";

    @LuaInterface
    static public String getInputString() {
        String ret = dialogInput;
        dialogInput = "";
        return ret;
    }

    @LuaInterface
    static public void showInputDialog(String title, String message, String defaultValue) {
    }

}
