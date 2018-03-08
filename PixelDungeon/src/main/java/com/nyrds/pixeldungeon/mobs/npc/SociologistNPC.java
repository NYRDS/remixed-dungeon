package com.nyrds.pixeldungeon.mobs.npc;

import android.Manifest;

import com.nyrds.android.util.DownloadStateListener;
import com.nyrds.android.util.DownloadTask;
import com.nyrds.android.util.FileSystem;
import com.nyrds.android.util.JsonHelper;
import com.watabou.noosa.Game;
import com.watabou.noosa.InterstitialPoint;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.windows.WndMessage;

import org.json.JSONException;

import java.io.File;

/**
 * Created by mike on 09.03.2018.
 * This file is part of Remixed Pixel Dungeon.
 */

public class SociologistNPC extends ImmortalNPC implements DownloadStateListener, InterstitialPoint {

    private static final String SURVEY_JSON = "survey.json";

    @Override
    public boolean interact(Hero hero) {

        String[] requiredPermissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET};
        Game.instance().doPermissionsRequest(this, requiredPermissions);

        return super.interact(hero);
    }

    @Override
    public void DownloadProgress(String file, Integer percent) {

    }

    @Override
    public void DownloadComplete(String file, final Boolean result) {
        Game.executeInGlThread(new Runnable() {
            @Override
            public void run() {
                if(!Game.isPaused()) {
                    if (!result) {
                        Game.toast("Survey list download failed :(");
                    } else {
                        try {
                            Game.scene().add(new WndMessage(JsonHelper.readJsonFromFile(FileSystem.getExternalStorageFile(SURVEY_JSON)).toString()));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    @Override
    public void returnToWork(boolean result) {
        if(result) {
            File survey = FileSystem.getExternalStorageFile(SURVEY_JSON);
            survey.delete();
            String downloadTo = survey.getAbsolutePath();

            new DownloadTask(this).download("https://github.com/NYRDS/pixel-dungeon-remix-survey/raw/master/survey.json", downloadTo);
        } else {
            say("No internet - No surveys");
        }
    }
}
