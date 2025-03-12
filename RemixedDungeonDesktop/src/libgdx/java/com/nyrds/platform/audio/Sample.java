package com.nyrds.platform.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.storage.FileSystem;
import com.nyrds.util.ModdingMode;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum Sample {

	INSTANCE;

	@NotNull
	private final Set<String> missingAssets = new HashSet<>();

	@NotNull
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
				Sound sound = Gdx.audio.newSound(FileSystem.getInternalStorageFileHandle(assetFile));
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
		GameLoop.instance().soundExecutor.execute(() -> {
			try {
				Sound sound = sounds.get(id);

				if (sound == null) {
					load(id);
				}

				sound = sounds.get(id);

				if (sound != null) {
					float volume = leftVolume * GamePreferences.soundFxVolume() / 10f;
					long s_id = sound.play(volume, rate, 0);
				} else {
					EventCollector.logException("Sound " + id + " not found");
				}
			} catch (Exception e) {
				EventCollector.logException(e, id);
			}
		});
	}

	public void enable(boolean value) {
		enabled = value;
	}
}