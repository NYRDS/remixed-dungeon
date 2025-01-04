
package com.nyrds.platform.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.storage.FileSystem;
import com.nyrds.util.ModdingMode;
import com.nyrds.util.Util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum MusicManager {

	INSTANCE;

	@Nullable
	private Music player;

	@Nullable
	private String lastPlayed;
	private boolean lastLooping;

	private boolean enabled = true;

	public void play(@NotNull String assetName, boolean looping) {
		if (!enabled) {
			lastPlayed = assetName;
			return;
		}

		if (isPlaying() && assetName.equals(lastPlayed)) {
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
			player = Gdx.audio.newMusic(FileSystem.getInternalStorageFileHandle(assetFilename));
			player.setLooping(looping);
			player.play();
			//PUtil.slog("music", "playing " + assetFilename);
		} catch (Exception e) {
			if (player != null) {
				player.dispose();
				player = null;
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
			player.pause();
		}
	}

	public void resume() {
		if (isPlaying()) {
			player.play();
		}
	}

	public void stop() {
		if (player != null) {
			player.stop();
			player.dispose();
			player = null;
		}
	}

	public void volume(float value) {
		try {
			if (player != null) {
				value *= GamePreferences.musicVolume() / 10f;
				value = Util.clamp(value, 0, 1);
				player.setVolume(value);
			}
		} catch (Exception e) {
			EventCollector.logException(e);
		}
	}

	public boolean isPlaying() {
		try {
			return player != null && player.isPlaying();
		} catch (Exception e) {
			EventCollector.logException(e);
		}
		return false;
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
}