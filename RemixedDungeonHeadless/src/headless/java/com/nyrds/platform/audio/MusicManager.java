package com.nyrds.platform.audio;

import com.nyrds.LuaInterface;
import org.jetbrains.annotations.NotNull;

// headless: silent
@LuaInterface
public enum MusicManager {

	INSTANCE;

	private boolean enabled = true;

	public void play(@NotNull String assetName, boolean looping) {
	}

	public void mute() {
	}

	public void pause() {
	}

	public void resume() {
	}

	public void volume(float vl) {
	}

	public void enable(boolean value) {
		enabled = value;
	}
}
