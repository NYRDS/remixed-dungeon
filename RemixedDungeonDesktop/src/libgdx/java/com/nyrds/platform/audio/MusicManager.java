
package com.nyrds.platform.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.storage.FileSystem;
import com.nyrds.util.ModdingMode;
import com.nyrds.util.Util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum MusicManager {

    INSTANCE;

    @Nullable
    private Music music;

    @Nullable
    private String lastPlayed;
    private boolean lastLooping;

    private boolean enabled = true;

    private float volume = 1;

    public void play(@NotNull String assetName, boolean looping) {
        Game.runOnUiThread(() -> {
            if (!enabled) {
                lastPlayed = assetName;
                return;
            }

            if (isPlaying() && assetName.equals(lastPlayed)) {
                volume(1);
                return;
            }

            String assetFilename = ModdingMode.getSoundById("sound/" + assetName);

            if (assetFilename.isEmpty()) {
                return;
            }

            stop();

            lastPlayed = assetName;
            lastLooping = looping;

            try {
                music = Gdx.audio.newMusic(FileSystem.getInternalStorageFileHandle(assetFilename));
                music.setLooping(looping);
                volume(1);
                music.play();
                //PUtil.slog("music", "playing " + assetFilename);
            } catch (Exception e) {
                if (music != null) {
                    music.dispose();
                    music = null;
                }
                EventCollector.logException(e, assetName);
            }
        });
    }

    public void mute() {
        lastPlayed = null;
        stop();
    }

    public void pause() {
        Game.runOnUiThread( () -> {
            if (isPlaying()) {
                music.pause();
            }
        });
    }

    public void resume() {
        Game.runOnUiThread(() -> {


            if (music != null) {
                volume(volume);
                music.play();
            }
        });
    }



    public void volume(float vl) {
        final float value = vl;
        Game.runOnUiThread(() -> {
            volume = value;
            try {
                if (music != null) {
                    float val = value * GamePreferences.musicVolume() / 10f;
                    val = Util.clamp(val, 0, 1);
                    music.setVolume(val);
                }
            } catch (Exception e) {
                EventCollector.logException(e);
            }
        });
    }

    public void enable(boolean value) {
        Game.runOnUiThread(() -> {
            enabled = value;
            if (isPlaying() && !value) {
                stop();
            } else if (!isPlaying() && value) {
                if (lastPlayed != null) {
                    play(lastPlayed, lastLooping);
                }
            }
        });
    }

    private boolean isPlaying() {
        try {
            return music != null && music.isPlaying();
        } catch (Exception e) {
            EventCollector.logException(e);
        }
        return false;
    }

    private void stop() {
        if (music != null) {
            music.stop();
            music.dispose();
            music = null;
        }
    }

}