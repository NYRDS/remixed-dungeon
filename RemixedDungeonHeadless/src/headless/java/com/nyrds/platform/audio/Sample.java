package com.nyrds.platform.audio;

import com.nyrds.LuaInterface;
import org.jetbrains.annotations.NotNull;

// headless: silent
@LuaInterface
public enum Sample {

	INSTANCE;

	@NotNull
	private final java.util.Set<String> missingAssets = new java.util.HashSet<>();

	private boolean enabled = true;

	public void reset() {
	}

	public void pause() {
	}

	public void resume() {
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
		if (!missingAssets.contains(id)) {
			missingAssets.add(id); // caveman: pretend it was looked up and is missing - silent build
		}
	}

	public void enable(boolean value) {
		enabled = value;
	}
}
