
package com.nyrds.platform.audio;

import org.jetbrains.annotations.NotNull;

public enum Music  {

	INSTANCE;


	public void play(@NotNull String assetName, boolean looping) {
	}

	public void mute() {
	}

	public void pause() {
	}

	public void resume() {
	}

	public void stop() {
	}

	public void volume(float value) {
	}

	public boolean isPlaying() {
		return false;
	}

	public void enable(boolean value) {
	}
}
