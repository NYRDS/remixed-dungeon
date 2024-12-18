package com.nyrds.platform.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.EventCollector;
import com.nyrds.util.ModdingMode;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum Sample {

	INSTANCE;

	public static final int MAX_STREAMS = 8;
	String playOnComplete;

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

				Sound sound = Gdx.audio.newSound(Gdx.files.internal("../assets/" +	assetFile));


				sounds.put(asset, sound);

			} catch (Exception e) {
				missingAssets.add(asset);
				playOnComplete = null;
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
			Sound sound = sounds.get(id);
			//PUtil.slog("sound", "playing " + id);
			if (sound != null) {
				sound.play(leftVolume, rate, 0);
			} else {
				playOnComplete = id;
				GameLoop.execute(() -> load(id));
			}
		});
	}

	public void enable(boolean value) {
		enabled = value;
	}

	public void onLoadComplete(String id) {
		if (playOnComplete != null && playOnComplete.equals(id)) {
			play(playOnComplete);
			playOnComplete = null;
		}
	}
}