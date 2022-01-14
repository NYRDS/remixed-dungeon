package com.nyrds.pixeldungeon.mobs.npc;

import android.Manifest;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.windows.DownloadProgressWindow;
import com.nyrds.pixeldungeon.windows.WndSurvey;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.storage.FileSystem;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.DownloadStateListener;
import com.nyrds.util.DownloadTask;
import com.nyrds.util.JsonHelper;
import com.watabou.noosa.InterstitialPoint;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.windows.WndError;
import com.watabou.pixeldungeon.windows.WndOptions;

import org.jetbrains.annotations.Nullable;

import java.io.File;

import clone.org.json.JSONException;
import clone.org.json.JSONObject;

/**
 * Created by mike on 09.03.2018.
 * This file is part of Remixed Pixel Dungeon.
 */

public class SociologistNPC extends ImmortalNPC implements DownloadStateListener.IDownloadComplete, InterstitialPoint {

    private static final String SURVEY_JSON = "survey.json";

    @Nullable
    private JSONObject survey;

    public boolean interact(Char hero) {

        GameLoop.addToScene(new WndOptions(this.name,
                StringsManager.getVar(R.string.SociologistNPC_Hi),
                StringsManager.getVar(R.string.Wnd_Button_Yes), StringsManager.getVar(R.string.Wnd_Button_No)
        ) {

            @Override
            public void onSelect(int index) {
                if (index == 0) {
                    String[] requiredPermissions = {Manifest.permission.INTERNET};
                    Game.instance().doPermissionsRequest(SociologistNPC.this, requiredPermissions);
                }
            }
        });
        return true;
    }


    @Override
    public void DownloadComplete(String file, final Boolean result) {
        GameLoop.pushUiTask(() -> {
            if (!result) {
                reportError();
            } else {
                try {
                    survey = JsonHelper.readJsonFromFile(FileSystem.getInternalStorageFile(SURVEY_JSON));

                    GameLoop.addToScene(new WndSurvey(survey));

                } catch (JSONException e) {
                    reportError();
                }
            }
        });
    }

    private void reportError() {
        GameLoop.addToScene(new WndError(StringsManager.getVar(R.string.SociologistNPC_DownloadError)));
    }

    @Override
    public void returnToWork(boolean result) {
        if (result) {
            File survey = FileSystem.getInternalStorageFile(SURVEY_JSON);
            survey.delete();
            String downloadTo = survey.getAbsolutePath();

            new DownloadTask(new DownloadProgressWindow("Downloading", this),
                    "https://github.com/NYRDS/pixel-dungeon-remix-survey/raw/master/survey.json",
                    downloadTo);
        } else {
            say(StringsManager.getVar(R.string.SociologistNPC_InternetRequired));
        }
    }
}
