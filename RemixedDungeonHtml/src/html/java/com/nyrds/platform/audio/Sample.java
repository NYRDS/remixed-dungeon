package com.nyrds.platform.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

import java.util.HashMap;
import java.util.Map;

public enum Sample {
    INSTANCE;

    private Map<String, Sound> soundCache = new HashMap<>();
    private boolean isMuted = false;
    private float volume = 1.0f;

    public void play(String assetName) {
        play(assetName, 1, 1, 1);
    }

    public void play(String assetName, float volume) {
        play(assetName, volume, volume, 1);
    }

    public void play(String assetName, float leftVolume, float rightVolume, float rate) {
        if (isMuted) return;

        Sound sound = soundCache.get(assetName);
        if (sound == null) {
            try {
                FileHandle file = Gdx.files.internal(assetName);
                if (file.exists()) {
                    sound = Gdx.audio.newSound(file);
                    soundCache.put(assetName, sound);
                }
            } catch (Exception e) {
                // Failed to load sound
                return;
            }
        }

        if (sound != null) {
            // For HTML version, we'll use a simplified approach
            // since we can't control left/right volume and rate separately
            float combinedVolume = leftVolume * volume;
            sound.play(combinedVolume);
        }
    }

    public void mute() {
        isMuted = true;
    }

    public void unMute() {
        isMuted = false;
    }

    public boolean isMuted() {
        return isMuted;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public float getVolume() {
        return volume;
    }

    public void reset() {
        for (Sound sound : soundCache.values()) {
            sound.dispose();
        }
        soundCache.clear();
    }

    public void pause() {
        // In HTML, we can't pause individual sounds, but we can mute them
        mute();
    }

    public void resume() {
        // In HTML, we can unmute sounds
        unMute();
    }

    public void clearCache() {
        for (Sound sound : soundCache.values()) {
            sound.dispose();
        }
        soundCache.clear();
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