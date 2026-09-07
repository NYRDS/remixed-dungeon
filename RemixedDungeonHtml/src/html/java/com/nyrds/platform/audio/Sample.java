package com.nyrds.platform.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.platform.EventCollector;
import com.nyrds.util.ModdingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Desktop parity: resolves ids through ModdingMode.getSoundById and scales
 * by the sound-fx preference. Actual playback is WebAudio-backed.
 */
@LuaInterface
public enum Sample {

	INSTANCE;

	private final Set<String> missingAssets = new HashSet<>();

	private final Map<String, Sound> sounds = new HashMap<>();

	private boolean enabled = true;

	public void reset() {
		for (Sound sound : sounds.values()) {
			sound.dispose();
		}
		sounds.clear();
	}

	public void pause() {
		for (Sound sound : sounds.values()) {
			sound.pause();
		}
	}

	public void resume() {
		for (Sound sound : sounds.values()) {
			sound.resume();
		}
	}

	private void load(String asset) {
		if (!sounds.containsKey(asset) && !missingAssets.contains(asset)) {
			try {
				String assetFile = ModdingMode.getSoundById("sound/" + asset);
				if (assetFile.isEmpty()) {
					missingAssets.add(asset);
					return;
				}
				Sound sound = Gdx.audio.newSound(Gdx.files.internal(assetFile));
				sounds.put(asset, sound);
			} catch (Exception e) {
				missingAssets.add(asset);
				EventCollector.logException(e, asset);
			}
		}
	}

	public void play(String id) {
		play(id, 1, 1, 1);
	}

	public void play(String id, float volume) {
		play(id, volume, volume, 1);
	}

	public void play(String id, float leftVolume, float rightVolume, float rate) {
		if (!enabled) {
			return;
		}
		try {
			Sound sound = sounds.get(id);

			if (sound == null) {
				load(id);
				sound = sounds.get(id);
			}

			if (sound != null) {
				float volume = leftVolume * GamePreferences.soundFxVolume() / 10f;
				sound.play(volume, rate, 0);
			} else {
				EventCollector.logException("Sound " + id + " not found");
			}
		} catch (Exception e) {
			EventCollector.logException(e, id);
		}
	}

	public void enable(boolean value) {
		enabled = value;
	}

	// legacy lua-facing aliases
	public void mute() {
		enable(false);
	}

	public void unMute() {
		enable(true);
	}

	public boolean isMuted() {
		return !enabled;
	}

	public void clearCache() {
		reset();
	}
}
