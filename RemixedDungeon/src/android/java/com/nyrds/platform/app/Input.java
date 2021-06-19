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
        Activity activity = Game.instance();
        activity.runOnUiThread(() -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    final EditText input = new EditText(activity);

                    if(defaultValue!=null) {
                        input.setText(defaultValue);
                    }

                    builder
                            .setTitle(title)
                            .setMessage(message)
                            .setView(input)
                            .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                                dialogInput = input.getText().toString();

                                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                            });

                    builder.show();
                    input.requestFocus();
                    InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                }
        );

    }

}
