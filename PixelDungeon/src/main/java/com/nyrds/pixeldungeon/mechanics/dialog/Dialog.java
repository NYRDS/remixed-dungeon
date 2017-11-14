package com.nyrds.pixeldungeon.mechanics.dialog;

import com.nyrds.android.util.JsonHelper;
import com.nyrds.android.util.TrackedRuntimeException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Dialog {

    public int imageIndex;

    public String dialogID;
    public String imagePath;
    public String title;
    public String text;
    public String portraitPos;

    public Dialog getFromJson(String targetID, int cardNum) {
        JSONObject initDialogs = JsonHelper.readJsonFromAsset("dialogDesc/Dialogs.json");
        if (initDialogs.has("dialogs")) {
            Dialog dialog = new Dialog();
            try {
                JSONArray dialogs = initDialogs.getJSONArray("dialogs");
                for (int i = 0; i < dialogs.length(); ++i) {
                    JSONObject currentDialog = dialogs.getJSONObject(i);
                    if (currentDialog.has("id")) {
                        String tempID = currentDialog.optString("id", "test");
                        if (tempID.equals(targetID)) {
                            dialog.dialogID = tempID;
                        }
                        break;
                    }
                    if (currentDialog.has("image")) {
                        dialog.imagePath = currentDialog.optString("image", "mobs/dialogAvatars.png");
                    }
                    if (currentDialog.has("dialogCards")) {
                        JSONArray cards = currentDialog.getJSONArray("dialogCards");
                        for (int j = 0; j < cards.length(); ++j) {
                            if(j == cardNum) {
                                dialog.imageIndex = cards.getJSONObject(j).optInt("imageIndex", 0);
                                dialog.portraitPos = cards.getJSONObject(j).optString("portraitPos", "right");
                                dialog.title = cards.getJSONObject(j).optString("title", "N/A");
                                dialog.text = cards.getJSONObject(j).optString("text", "No text found");
                                return dialog;
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                throw new TrackedRuntimeException(e);
            }
        }
        return null;
    }
}
