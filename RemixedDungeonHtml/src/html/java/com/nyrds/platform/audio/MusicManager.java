package com.nyrds.platform.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;

import java.util.HashMap;
import java.util.Map;

public enum MusicManager {
    INSTANCE;

    private Map<String, Music> musicCache = new HashMap<>();
    private Music currentMusic;
    private boolean isMuted = false;
    private float volume = 1.0f;

    public void play(String assetName, boolean looping) {
        if (isMuted) return;

        stop(); // Stop any currently playing music

        Music music = musicCache.get(assetName);
        if (music == null) {
            try {
                FileHandle file = Gdx.files.internal(assetName);
                if (file.exists()) {
                    music = Gdx.audio.newMusic(file);
                    music.setLooping(looping);
                    musicCache.put(assetName, music);
                }
            } catch (Exception e) {
                // Failed to load music
                return;
            }
        }

        if (music != null) {
            currentMusic = music;
            currentMusic.setVolume(volume);
            currentMusic.play();
        }
    }

    public void stop() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic = null;
        }
    }

    public void pause() {
        if (currentMusic != null) {
            currentMusic.pause();
        }
    }

    public void resume() {
        if (currentMusic != null && !isMuted) {
            currentMusic.play();
        }
    }

    public void mute() {
        isMuted = true;
        if (currentMusic != null) {
            currentMusic.setVolume(0f);
        }
    }

    public void unMute() {
        isMuted = false;
        if (currentMusic != null) {
            currentMusic.setVolume(volume);
        }
    }

    public boolean isMuted() {
        return isMuted;
    }

    public void volume(float value) {
        this.volume = value;
        if (currentMusic != null) {
            currentMusic.setVolume(isMuted ? 0f : value);
        }
    }

    public void setVolume(float volume) {
        this.volume = volume;
        if (currentMusic != null) {
            currentMusic.setVolume(isMuted ? 0f : volume);
        }
    }

    public float getVolume() {
        return volume;
    }

    public boolean isPlaying() {
        return currentMusic != null && currentMusic.isPlaying();
    }

    public void clearCache() {
        stop();
        for (Music music : musicCache.values()) {
            music.dispose();
        }
        musicCache.clear();
    }

    // Method needed for HTML version
    public void enable(boolean value) {
        if (value) {
            unMute();
        } else {
            mute();
        }
    }
}