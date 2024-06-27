package com.nyrds.platform.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.nyrds.LuaInterface;
import com.nyrds.platform.game.Game;

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
