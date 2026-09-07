package com.nyrds.platform.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.platform.EventCollector;
import com.nyrds.util.ModdingMode;
import com.nyrds.util.Util;
import org.jetbrains.annotations.Nullable;

/**
 * Desktop parity: single music track, id resolved through
 * ModdingMode.getSoundById, volume scaled by the music preference.
 * Playback is WebAudio-backed; decoded buffers are cached by WebAudio so
 * scene switches don't re-fetch.
 */
@LuaInterface
public enum MusicManager {

	INSTANCE;

	@Nullable
	private Music music;

	@Nullable
	private String lastPlayed;
	private boolean lastLooping;

	private boolean enabled = true;

	private float volume = 1;

	public void play(String assetName, boolean looping) {
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
			music = Gdx.audio.newMusic(Gdx.files.internal(assetFilename));
			music.setLooping(looping);
			volume(1);
			music.play();
		} catch (Exception e) {
			if (music != null) {
				music.dispose();
				music = null;
			}
			EventCollector.logException(e, assetName);
		}
	}

	public void mute() {
		lastPlayed = null;
		stop();
	}

	public void pause() {
		if (isPlaying()) {
			music.pause();
		}
	}

	public void resume() {
		if (music != null && enabled) {
			volume(volume);
			music.play();
		}
	}

	public void volume(float vl) {
		volume = vl;
		try {
			if (music != null) {
				float val = value(vl);
				music.setVolume(val);
			}
		} catch (Exception e) {
			EventCollector.logException(e);
		}
	}

	private float value(float vl) {
		float val = vl * GamePreferences.musicVolume() / 10f;
		return Util.clamp(val, 0, 1);
	}

	public void enable(boolean value) {
		enabled = value;
		if (isPlaying() && !value) {
			stop();
		} else if (!isPlaying() && value) {
			if (lastPlayed != null) {
				play(lastPlayed, lastLooping);
			}
		}
	}

	public boolean isMuted() {
		return !enabled;
	}

	public boolean isPlaying() {
		try {
			return music != null && music.isPlaying();
		} catch (Exception e) {
			EventCollector.logException(e);
		}
		return false;
	}

	public void clearCache() {
		stop();
	}

	private void stop() {
		if (music != null) {
			music.stop();
			music.dispose();
			music = null;
		}
	}
}
